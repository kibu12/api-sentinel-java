package com.apisentinel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final StringRedisTemplate redisTemplate;

    // In-memory fallback cache entry
    private record CacheEntry(String value, Instant expiresAt) {}
    private final Map<String, CacheEntry> fallbackCache = new ConcurrentHashMap<>();

    public CacheService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateKey(UUID apiId, String method, String path, String body) {
        String payload = (method != null ? method.toUpperCase() : "POST") + ":" +
                (path != null ? path : "") + ":" +
                (body != null ? body : "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return "api:" + apiId + ":req:" + hex;
        } catch (NoSuchAlgorithmException e) {
            return "api:" + apiId + ":req:" + Integer.toHexString(payload.hashCode());
        }
    }

    public Optional<String> get(String key) {
        if (redisTemplate != null) {
            try {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("Redis CACHE HIT for [{}]", key);
                    return Optional.of(value);
                }
            } catch (Exception e) {
                log.warn("Redis unavailable, falling back to in-memory cache: {}", e.getMessage());
            }
        }

        CacheEntry entry = fallbackCache.get(key);
        if (entry != null) {
            if (entry.expiresAt.isAfter(Instant.now())) {
                log.debug("Fallback in-memory CACHE HIT for [{}]", key);
                return Optional.of(entry.value);
            } else {
                fallbackCache.remove(key);
            }
        }

        return Optional.empty();
    }

    public void put(String key, String value, int ttlSeconds) {
        int boundedTtl = Math.max(1, Math.min(ttlSeconds, 86400)); // Cap at 24h
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(boundedTtl));
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable during put, caching in-memory: {}", e.getMessage());
            }
        }

        fallbackCache.put(key, new CacheEntry(value, Instant.now().plusSeconds(boundedTtl)));
    }
}
