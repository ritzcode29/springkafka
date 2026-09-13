package com.banking.account;

import com.banking.account.entity.Account;
import com.banking.account.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
@EnableCaching
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(AccountRepository repo) {
        return args -> {
            repo.save(new Account("ACC101", "Alice", new BigDecimal("5000.00"), "ACTIVE"));
            repo.save(new Account("ACC102", "Bob", new BigDecimal("1000.00"), "ACTIVE"));
        };
    }
}