package com.apisentinel.resilience;

import com.apisentinel.exception.CircuitOpenException;
import com.apisentinel.exception.UpstreamTimeoutException;
import com.apisentinel.exception.UpstreamUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Callable;

@Service
public class ResilienceService {

    private static final Logger log = LoggerFactory.getLogger(ResilienceService.class);
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceService(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public <T> T executeWithResilience(UUID apiId, Callable<T> callable) throws Exception {
        String instanceName = "api-" + apiId;
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceName, "upstreamGateway");

        try {
            return circuitBreaker.executeCallable(callable);
        } catch (CallNotPermittedException e) {
            log.warn("Resilience4j Circuit Breaker is OPEN for API [{}]", apiId);
            throw new CircuitOpenException("Circuit breaker is OPEN for upstream API. Traffic is temporarily isolated to protect the system.");
        } catch (UpstreamTimeoutException | UpstreamUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Resilience execution failed for API [{}]: {}", apiId, e.getMessage());
            throw e;
        }
    }

    public CircuitBreaker.State getCircuitBreakerState(UUID apiId) {
        String instanceName = "api-" + apiId;
        return circuitBreakerRegistry.circuitBreaker(instanceName, "upstreamGateway").getState();
    }
}
