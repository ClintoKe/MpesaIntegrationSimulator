package com.example.mpesa.repository;

import com.example.mpesa.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);
}
