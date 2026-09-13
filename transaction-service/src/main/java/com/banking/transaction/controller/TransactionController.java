package com.banking.transaction.controller;

import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.entity.BankTransaction;
import com.banking.transaction.service.TransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public BankTransaction transfer(@RequestBody TransferRequest req) {
        return transactionService.initiateTransfer(req);
    }

    @GetMapping("/{id}")
    public BankTransaction getTransaction(@PathVariable("id") String id) {
        return transactionService.getTransaction(id);
    }
}