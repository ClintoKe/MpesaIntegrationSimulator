package com.example.mpesa.dto;

import com.example.mpesa.model.TransactionStatus;

import java.util.UUID;

public record StatusResponse(
        UUID transactionId,
        TransactionStatus status,
        String resultCode,
        String resultDesc
) {}
