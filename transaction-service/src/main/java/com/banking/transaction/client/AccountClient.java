package com.banking.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "account-service", url = "http://localhost:8081", fallback = AccountClientFallback.class)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    Map<String, Object> getAccount(@PathVariable("id") String id);
}