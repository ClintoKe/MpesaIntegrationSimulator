package com.example.mpesa.controller;

import com.example.mpesa.dto.daraja.StkPushQueryRequest;
import com.example.mpesa.dto.daraja.StkPushQueryResponse;
import com.example.mpesa.dto.daraja.StkPushRequest;
import com.example.mpesa.dto.daraja.StkPushResponse;
import com.example.mpesa.model.Transaction;
import com.example.mpesa.model.TransactionStatus;
import com.example.mpesa.service.MpesaSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/mpesa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DarajaMockController {

    private final MpesaSimulationService service;

    // Mimic Daraja STK Push: /mpesa/stkpush/v1/processrequest
    @PostMapping("/stkpush/v1/processrequest")
    public ResponseEntity<StkPushResponse> processRequest(@Valid @RequestBody StkPushRequest req) {
        // Map Daraja request to our internal initiate request
        var internal = new com.example.mpesa.dto.InitiateRequest(
                req.PhoneNumber(),
                req.Amount(),
                Optional.ofNullable(req.TransactionDesc()).filter(s -> !s.isBlank()).orElse("Payment")
        );
        Transaction tx = service.initiateStkPush(internal);

        String merchantRequestId = "MRID-" + tx.getId();
        String checkoutRequestId = tx.getCheckoutRequestId();

        var response = new StkPushResponse(
                merchantRequestId,
                checkoutRequestId,
                "0",
                "Success. Request accepted for processing",
                "Success. Request accepted for processing"
        );
        return ResponseEntity.ok(response);
    }

    // Mimic Daraja STK Query: /mpesa/stkpushquery/v1/query
    @PostMapping("/stkpushquery/v1/query")
    public ResponseEntity<StkPushQueryResponse> query(@Valid @RequestBody StkPushQueryRequest req) {
        Transaction tx = service.getByCheckoutRequestId(req.CheckoutRequestID());

        String responseCode = "0"; // API call success
        String responseDescription = "The service request is processed successfully.";
        String resultCode;
        String resultDesc;
        if (tx.getStatus() == TransactionStatus.PROCESSING) {
            resultCode = "1001"; // mock code for processing/pending
            resultDesc = "The transaction is being processed.";
        } else if (tx.getStatus() == TransactionStatus.SUCCESS) {
            resultCode = "0";
            resultDesc = "The service request is processed successfully.";
        } else {
            resultCode = Optional.ofNullable(tx.getResultCode()).orElse("1");
            resultDesc = Optional.ofNullable(tx.getResultDesc()).orElse("The service request failed.");
        }

        var response = new StkPushQueryResponse(
                responseCode,
                responseDescription,
                "MRID-" + tx.getId(),
                tx.getCheckoutRequestId(),
                resultCode,
                resultDesc
        );
        return ResponseEntity.ok(response);
    }
}
