package com.banking.transaction.client;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountClientFallback implements AccountClient {

    @Override
    public Map<String, Object> getAccount(String id) {
        System.err.println(">>> [CIRCUIT BREAKER FALLBACK] Account Service unreachable for ID: " + id);
        return Map.of("status", "UNAVAILABLE", "accountId", id, "balance", 0);
    }
}