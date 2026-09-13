package com.banking.transaction.service;

import com.banking.transaction.exception.DuplicateRequestException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically acquires a lock on the idempotency key using Redis SETNX (setIfAbsent).
     *
     * @param idempotencyKey Key passed in header from client
     * @param ttlSeconds     Expiration duration for the key
     */
    public void validateAndLock(String idempotencyKey, long ttlSeconds) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return; // Skip validation if header is not supplied, or throw an exception if required
        }

        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;

        // Redis SETNX: Returns true if key was set, false if key already exists
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", Duration.ofSeconds(ttlSeconds));

        if (Boolean.FALSE.equals(acquired)) {
            throw new DuplicateRequestException(
                    "Duplicate request detected for Idempotency-Key: " + idempotencyKey
            );
        }
    }

    /**
     * Updates key status to COMPLETED once processing succeeds.
     */
    public void markCompleted(String idempotencyKey, long ttlSeconds) {
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
            redisTemplate.opsForValue().set(redisKey, "COMPLETED", Duration.ofSeconds(ttlSeconds));
        }
    }
}