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

        // Do not schedule any automatic callback. Outcome will be decided by user action (approve/cancel).
        return tx;
    }

    @Transactional
    public void handleCallback(CallbackPayload payload) {
        Transaction tx = transactionRepository.findByCheckoutRequestId(payload.checkoutRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found for checkoutRequestId"));

        // Only apply callback if still processing AND no user path has started (resultDesc still null)
        if (tx.getStatus() == TransactionStatus.PROCESSING && tx.getResultDesc() == null) {
            tx.setResultCode(payload.resultCode());
            tx.setResultDesc(payload.resultDesc());
            tx.setStatus("0".equals(payload.resultCode()) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
            transactionRepository.save(tx);
        }
    }

    @Transactional(readOnly = true)
    public Transaction get(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    @Transactional(readOnly = true)
    public Transaction getByCheckoutRequestId(String checkoutRequestId) {
        return transactionRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found for checkoutRequestId"));
    }

    @Transactional
    public Transaction approve(UUID id, String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("Invalid PIN format");
        }
        Transaction tx = get(id);
        if (tx.getStatus() == TransactionStatus.PROCESSING) {
            // Immediately approve as SUCCESS
            tx.setResultCode("0");
            tx.setResultDesc("User approved with PIN");
            tx.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(tx);
        }
        return tx;
    }

    @Transactional
    public void completeApproval(UUID id) {
        Transaction t = get(id);
        // Only mark success if still processing (not cancelled in the meantime)
        if (t.getStatus() == TransactionStatus.PROCESSING) {
            t.setResultCode("0");
            t.setResultDesc("User approved with PIN");
            t.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(t);
        }
    }

    @Transactional
    public Transaction cancel(UUID id) {
        Transaction tx = get(id);
        if (tx.getStatus() == TransactionStatus.PROCESSING) {
            tx.setResultCode("1");
            tx.setResultDesc("User cancelled");
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
        }
        return tx;
    }

    private void simulateCallback(UUID txId, String checkoutRequestId) {
        // Deprecated: no automatic callbacks. Left in place in case of future simulation needs.
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
