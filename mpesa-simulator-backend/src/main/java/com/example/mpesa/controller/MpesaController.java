package com.example.mpesa.controller;

import com.example.mpesa.dto.CallbackPayload;
import com.example.mpesa.dto.InitiateRequest;
import com.example.mpesa.dto.InitiateResponse;
import com.example.mpesa.dto.StatusResponse;
import com.example.mpesa.model.Transaction;
import com.example.mpesa.service.MpesaSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MpesaController {

    private final MpesaSimulationService service;

    @PostMapping("/initiate")
    public ResponseEntity<InitiateResponse> initiate(@Valid @RequestBody InitiateRequest request) {
        Transaction tx = service.initiateStkPush(request);
        return ResponseEntity.ok(new InitiateResponse(
                tx.getId(),
                tx.getCheckoutRequestId(),
                "STK Push initiated. Awaiting callback..."
        ));
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody CallbackPayload payload) {
        service.handleCallback(payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<StatusResponse> approve(@PathVariable("id") UUID id, @RequestParam("pin") String pin) {
        Transaction tx = service.approve(id, pin);
        return ResponseEntity.ok(new StatusResponse(tx.getId(), tx.getStatus(), tx.getResultCode(), tx.getResultDesc()));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<StatusResponse> cancel(@PathVariable("id") UUID id) {
        Transaction tx = service.cancel(id);
        return ResponseEntity.ok(new StatusResponse(tx.getId(), tx.getStatus(), tx.getResultCode(), tx.getResultDesc()));
    }

    // Optional helper to let frontend check result status
    @GetMapping("/status/{id}")
    public ResponseEntity<StatusResponse> status(@PathVariable("id") UUID id) {
        Transaction tx = service.get(id);
        return ResponseEntity.ok(new StatusResponse(
                tx.getId(), tx.getStatus(), tx.getResultCode(), tx.getResultDesc()
        ));
    }
}
