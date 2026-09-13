package com.banking.transaction.service;

import com.banking.transaction.client.AccountClient;
import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.entity.BankTransaction;
import com.banking.transaction.repository.TransactionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountClient accountClient,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public BankTransaction initiateTransfer(TransferRequest req) {
        // Feign Client synchronous check
        System.out.println(">>> [FEIGN CLIENT] Checking sender account: " + req.getFromAccountId());
        Map<String, Object> senderAccount = accountClient.getAccount(req.getFromAccountId());

        if ("UNAVAILABLE".equals(senderAccount.get("status"))) {
            throw new RuntimeException("Account Service is currently unavailable. Circuit breaker activated.");
        }

        // Persist initial PENDING record
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
        BankTransaction txn = new BankTransaction(
                txnId,
                req.getFromAccountId(),
                req.getToAccountId(),
                req.getAmount(),
                "PENDING",
                "SAGA transfer initiated over Kafka."
        );
        transactionRepository.save(txn);

        // Publish event to Kafka
        System.out.println(">>> [KAFKA PRODUCER] Emitting transfer-initiated event for: " + txnId);
        kafkaTemplate.send("transfer-initiated", Map.of(
                "transactionId", txnId,
                "fromAccountId", req.getFromAccountId(),
                "toAccountId", req.getToAccountId(),
                "amount", req.getAmount()
        ));

        return txn;
    }

    public BankTransaction getTransaction(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }
}