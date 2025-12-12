package com.example.mpesa.dto;

import java.util.UUID;

public record InitiateResponse(
        UUID transactionId,
        String checkoutRequestId,
        String message
) {}
