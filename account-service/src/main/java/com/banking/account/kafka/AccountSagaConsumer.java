package com.banking.account.kafka;

import com.banking.account.service.AccountService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountSagaConsumer {

    private final AccountService accountService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountSagaConsumer(AccountService accountService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "transfer-initiated", groupId = "account-saga-group")
    public void handleTransferInitiated(Map<String, Object> event) {
        String txnId = (String) event.get("transactionId");
        String from = (String) event.get("fromAccountId");
        String to = (String) event.get("toAccountId");
        BigDecimal amount = new BigDecimal(event.get("amount").toString());

        System.out.println(">>> [KAFKA CONSUMER] Account Service received transfer-initiated for Txn: " + txnId);
        try {
            accountService.executeTransfer(from, to, amount);
            System.out.println(">>> [KAFKA PRODUCER] Account Service emitting transfer-success");
            kafkaTemplate.send("transfer-success", Map.of("transactionId", txnId, "status", "COMPLETED"));
        } catch (Exception e) {
            System.err.println(">>> [KAFKA PRODUCER - SAGA COMPENSATION] Emitting transfer-failed: " + e.getMessage());
            kafkaTemplate.send("transfer-failed", Map.of("transactionId", txnId, "reason", e.getMessage()));
        }
    }
}