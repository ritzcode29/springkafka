package com.banking.transaction.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Fixed-window rate limiter using atomic Redis INCR and EXPIRE.
     *
     * @param clientId Identifier (e.g., account ID, user ID, or IP)
     * @param maxRequests Maximum allowed requests in the time window
     * @param windowSeconds Window length in seconds
     * @return true if allowed, false if limit exceeded
     */
    public boolean isAllowed(String clientId, long maxRequests, long windowSeconds) {
        String key = RATE_LIMIT_PREFIX + clientId;

        // Increment count atomically
        Long count = redisTemplate.opsForValue().increment(key);

        // If this is the first hit in the window, initialize the TTL
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return count != null && count <= maxRequests;
    }
}