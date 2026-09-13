package com.banking.account.service;

import com.banking.account.entity.Account;
import com.banking.account.repository.AccountRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Cacheable(value = "accounts", key = "#p0")
    public Account getAccount(String id) {
        System.out.println(">>> [REDIS CACHE MISS] Querying H2 database for account: " + id);
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#p0"),
            @CacheEvict(value = "accounts", key = "#p1")
    })
    @Transactional
    public void executeTransfer(String fromId, String toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + fromId));
        Account to = accountRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + toId));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account: " + fromId);
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
    }
}