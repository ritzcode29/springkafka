package com.banking.transaction.repository;

import com.banking.transaction.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<BankTransaction, String> {
}