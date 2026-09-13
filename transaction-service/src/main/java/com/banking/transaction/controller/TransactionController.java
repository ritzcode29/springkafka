package com.banking.transaction.controller;

import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.entity.BankTransaction;
import com.banking.transaction.service.IdempotencyService;
import com.banking.transaction.service.RateLimiterService;
import com.banking.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final RateLimiterService rateLimiterService;

    public TransactionController(TransactionService transactionService,
                                 IdempotencyService idempotencyService,
                                 RateLimiterService rateLimiterService) {
        this.transactionService = transactionService;
        this.idempotencyService = idempotencyService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequest req) {

        // 1. Rate Limit: Max 5 transfers per 60 seconds per source account
        boolean allowed = rateLimiterService.isAllowed(req.getFromAccountId(), 5, 60);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Maximum 5 transfer requests per minute.");
        }

        // 2. Distributed Idempotency: Reject duplicate attempts with the same key within 120 seconds
        idempotencyService.validateAndLock(idempotencyKey, 120);

        // 3. Initiate Transfer
        BankTransaction result = transactionService.initiateTransfer(req);

        // 4. Mark processing complete for the idempotency key
        idempotencyService.markCompleted(idempotencyKey, 120);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public BankTransaction getTransaction(@PathVariable("id") String id) {
        return transactionService.getTransaction(id);
    }
}