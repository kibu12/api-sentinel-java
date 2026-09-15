package com.apisentinel.config;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.api.ApiConfigurationRepository;
import com.apisentinel.apikey.ApiKey;
import com.apisentinel.apikey.ApiKeyRepository;
import com.apisentinel.apikey.ApiKeyService;
import com.apisentinel.application.Application;
import com.apisentinel.application.ApplicationRepository;
import com.apisentinel.auth.User;
import com.apisentinel.auth.UserRepository;
import com.apisentinel.budget.Budget;
import com.apisentinel.budget.BudgetRepository;
import com.apisentinel.cost.PricingRule;
import com.apisentinel.cost.PricingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApiConfigurationRepository apiRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ApplicationRepository applicationRepository,
            ApiConfigurationRepository apiRepository,
            ApiKeyRepository apiKeyRepository,
            PricingRuleRepository pricingRuleRepository,
            BudgetRepository budgetRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.apiRepository = apiRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data load.");
            return;
        }

        log.info("Seeding initial development demo data (AC-19, AC-20, §23)...");

        // 1. Seed Demo User
        User demoUser = new User();
        demoUser.setEmail("demo@apisentinel.dev");
        demoUser.setPasswordHash(passwordEncoder.encode("password123"));
        demoUser.setRole("ROLE_USER");
        demoUser.setStatus("ACTIVE");
        demoUser = userRepository.save(demoUser);

        // 2. Seed Admin User
        User adminUser = new User();
        adminUser.setEmail("admin@apisentinel.dev");
        adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setStatus("ACTIVE");
        userRepository.save(adminUser);

        // 3. Seed Application
        Application app = new Application();
        app.setOwner(demoUser);
        app.setName("Demo Cloud App");
        app.setEnvironment("DEVELOPMENT");
        app.setStatus("ACTIVE");
        app = applicationRepository.save(app);

        // 4. Seed API 1: Echo Mock API (Low-Cost, Cacheable)
        ApiConfiguration echoApi = new ApiConfiguration();
        echoApi.setApplication(app);
        echoApi.setName("Echo Mock API (Low Cost)");
        echoApi.setProvider("MOCK");
        echoApi.setBaseUrl("http://localhost:8080/mock-upstream");
        echoApi.setStatus("ACTIVE");
        echoApi.setRateLimitPerMinute(10); // 10 req/min for easy rate limit demonstration
        echoApi.setDailyQuota(50);
        echoApi.setMonthlyQuota(1000);
        echoApi.setDailyBudget(new BigDecimal("10.0000"));
        echoApi.setMonthlyBudget(new BigDecimal("200.0000"));
        echoApi.setTimeoutMs(3000);
        echoApi.setCacheEnabled(true);
        echoApi.setCacheTtlSeconds(60);
        echoApi = apiRepository.save(echoApi);

        // Pricing rule for Echo API ($0.002/request)
        PricingRule p1 = new PricingRule();
        p1.setApiConfiguration(echoApi);
        p1.setPricingType("PER_REQUEST");
        p1.setRequestPrice(new BigDecimal("0.002000"));
        p1.setEffectiveFrom(Instant.now());
        p1.setActive(true);
        pricingRuleRepository.save(p1);

        // Budgets for Echo API
        Budget b1 = new Budget();
        b1.setApiConfiguration(echoApi);
        b1.setPeriodType("DAILY");
        b1.setLimitAmount(new BigDecimal("10.0000"));
        b1.setWarningPercent(80);
        b1.setCriticalPercent(90);
        b1.setBlockingEnabled(true);
        budgetRepository.save(b1);

        // 5. Seed API 2: Unstable / Flaky Test API (for Resilience demo)
        ApiConfiguration flakyApi = new ApiConfiguration();
        flakyApi.setApplication(app);
        flakyApi.setName("Unstable Upstream API (Resilience Test)");
        flakyApi.setProvider("MOCK");
        flakyApi.setBaseUrl("http://localhost:8080/mock-upstream");
        flakyApi.setStatus("ACTIVE");
        flakyApi.setRateLimitPerMinute(60);
        flakyApi.setDailyQuota(500);
        flakyApi.setMonthlyQuota(10000);
        flakyApi.setDailyBudget(new BigDecimal("25.0000"));
        flakyApi.setMonthlyBudget(new BigDecimal("500.0000"));
        flakyApi.setTimeoutMs(1000);
        flakyApi.setCacheEnabled(false);
        flakyApi.setCacheTtlSeconds(30);
        flakyApi = apiRepository.save(flakyApi);

        PricingRule p2 = new PricingRule();
        p2.setApiConfiguration(flakyApi);
        p2.setPricingType("PER_REQUEST");
        p2.setRequestPrice(new BigDecimal("0.005000"));
        p2.setEffectiveFrom(Instant.now());
        p2.setActive(true);
        pricingRuleRepository.save(p2);

        Budget b2 = new Budget();
        b2.setApiConfiguration(flakyApi);
        b2.setPeriodType("DAILY");
        b2.setLimitAmount(new BigDecimal("25.0000"));
        b2.setWarningPercent(80);
        b2.setCriticalPercent(90);
        b2.setBlockingEnabled(true);
        budgetRepository.save(b2);

        // 6. Seed Known Demo API Key: "sen_live_demo_key_1234567890abcdef"
        String rawDemoKey = "sen_live_demo_key_1234567890abcdef";
        String keyHash = ApiKeyService.hashKey(rawDemoKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setApplication(app);
        apiKey.setKeyPrefix("sen_live_demo_key...");
        apiKey.setKeyHash(keyHash);
        apiKey.setStatus("ACTIVE");
        apiKeyRepository.save(apiKey);

        log.info("==================================================================");
        log.info("DEMO SEED DATA INITIALIZED:");
        log.info("Demo User: demo@apisentinel.dev / password123");
        log.info("Admin User: admin@apisentinel.dev / admin123");
        log.info("Demo API Key: {}", rawDemoKey);
        log.info("Seed API 1 (Echo): ID = {}", echoApi.getId());
        log.info("Seed API 2 (Flaky): ID = {}", flakyApi.getId());
        log.info("==================================================================");
    }
}
