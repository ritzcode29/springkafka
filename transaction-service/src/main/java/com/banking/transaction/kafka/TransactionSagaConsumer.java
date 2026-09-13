package com.banking.transaction.kafka;

import com.banking.transaction.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionSagaConsumer {

    private final TransactionRepository transactionRepository;

    public TransactionSagaConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "transfer-success", groupId = "transaction-saga-group")
    public void handleTransferSuccess(Map<String, Object> event) {
        String txnId = (String) event.get("transactionId");
        System.out.println(">>> [KAFKA CONSUMER] SAGA Success event received for Txn: " + txnId);
        transactionRepository.findById(txnId).ifPresent(txn -> {
            txn.setStatus("COMPLETED");
            txn.setMessage("SAGA completed successfully.");
            transactionRepository.save(txn);
        });
    }

    @KafkaListener(topics = "transfer-failed", groupId = "transaction-saga-group")
    public void handleTransferFailed(Map<String, Object> event) {
        String txnId = (String) event.get("transactionId");
        String reason = (String) event.get("reason");
        System.err.println(">>> [KAFKA CONSUMER] SAGA Failure event received for Txn: " + txnId);
        transactionRepository.findById(txnId).ifPresent(txn -> {
            txn.setStatus("FAILED");
            txn.setMessage("Compensation triggered: " + reason);
            transactionRepository.save(txn);
        });
    }
}