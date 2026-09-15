package com.apisentinel.cost;

import com.apisentinel.api.ApiConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {
    List<PricingRule> findByApiConfigurationAndActiveTrue(ApiConfiguration apiConfiguration);
    Optional<PricingRule> findFirstByApiConfigurationAndActiveTrueOrderByEffectiveFromDesc(ApiConfiguration apiConfiguration);
}
