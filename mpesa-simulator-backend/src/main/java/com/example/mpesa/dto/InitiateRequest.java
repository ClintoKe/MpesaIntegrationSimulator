package com.example.mpesa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record InitiateRequest(
        @NotBlank
        @Pattern(regexp = "^(?:\\+?254|0)7\\d{8}$", message = "Phone must be Kenyan format, e.g., 07XXXXXXXX or +2547XXXXXXXX")
        String phoneNumber,
        @NotNull @DecimalMin(value = "1.00", inclusive = true)
        BigDecimal amount,
        @NotBlank
        String purpose
) {}
