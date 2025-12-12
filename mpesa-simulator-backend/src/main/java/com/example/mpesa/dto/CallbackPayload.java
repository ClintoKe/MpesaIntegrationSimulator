package com.example.mpesa.dto;

public record CallbackPayload(
        String checkoutRequestId,
        String resultCode,
        String resultDesc
) {}
