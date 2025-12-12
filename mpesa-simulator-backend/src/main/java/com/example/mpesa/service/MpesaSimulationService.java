package com.example.mpesa.service;

import com.example.mpesa.dto.CallbackPayload;
import com.example.mpesa.dto.InitiateRequest;
import com.example.mpesa.model.Transaction;
import com.example.mpesa.model.TransactionStatus;
import com.example.mpesa.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MpesaSimulationService {

    private final TransactionRepository transactionRepository;
    private final TaskScheduler taskScheduler;
    private final Random random = new Random();

    @Transactional
    public Transaction initiateStkPush(InitiateRequest request) {
        // create a transaction with PROCESSING status
        Transaction tx = Transaction.builder()
                .phoneNumber(normalizePhone(request.phoneNumber()))
                .amount(request.amount())
                .purpose(request.purpose())
                .status(TransactionStatus.PROCESSING)
                .checkoutRequestId("CRID-" + UUID.randomUUID())
                .build();
        tx = transactionRepository.save(tx);

        // schedule a simulated callback after 2 seconds
        var when = Instant.now().plusSeconds(2);
        UUID txId = tx.getId();
        String checkoutRequestId = tx.getCheckoutRequestId();
        taskScheduler.schedule(() -> simulateCallback(txId, checkoutRequestId), when);

        return tx;
    }

    @Transactional
    public void handleCallback(CallbackPayload payload) {
        Transaction tx = transactionRepository.findByCheckoutRequestId(payload.checkoutRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found for checkoutRequestId"));

        tx.setResultCode(payload.resultCode());
        tx.setResultDesc(payload.resultDesc());
        tx.setStatus("0".equals(payload.resultCode()) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Transaction get(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    private void simulateCallback(UUID txId, String checkoutRequestId) {
        // Randomly determine success (70%) or failure (30%)
        boolean success = random.nextDouble() < 0.7;
        String resultCode = success ? "0" : "1";
        String resultDesc = success ? "The service request is processed successfully." : "The service request failed.";

        // Use service method to update state (transactional)
        handleCallback(new CallbackPayload(checkoutRequestId, resultCode, resultDesc));
    }

    private String normalizePhone(String phone) {
        String p = phone.trim();
        if (p.startsWith("0")) {
            return "+254" + p.substring(1);
        }
        if (!p.startsWith("+")) {
            return "+" + p;
        }
        return p;
    }
}
