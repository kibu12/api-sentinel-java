package com.apisentinel.ratelimit;

import com.apisentinel.api.ApiConfiguration;
import com.apisentinel.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    // In-memory token buckets keyed by API or API+Key
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkRateLimit(ApiConfiguration api, UUID apiKeyId) {
        String bucketKey = "api:" + api.getId() + (apiKeyId != null ? ":key:" + apiKeyId : "");
        int capacity = Math.max(1, api.getRateLimitPerMinute());

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createNewBucket(capacity));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
            log.warn("Rate limit breached for [{}]. Retry after {}s", bucketKey, retryAfterSeconds);
            throw new RateLimitExceededException(
                    "Rate limit of " + capacity + " requests/minute exceeded for API '" + api.getName() + "'. Retry after " + retryAfterSeconds + "s",
                    retryAfterSeconds
            );
        }
    }

    private Bucket createNewBucket(int capacity) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public void clearBucket(String key) {
        buckets.remove(key);
    }
}
