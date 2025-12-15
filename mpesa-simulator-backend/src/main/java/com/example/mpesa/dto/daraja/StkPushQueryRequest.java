package com.example.mpesa.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record StkPushQueryRequest(
        @JsonProperty("BusinessShortCode") @NotBlank String BusinessShortCode,
        @JsonProperty("Password") @NotBlank String Password,
        @JsonProperty("Timestamp") @NotBlank String Timestamp,
        @JsonProperty("CheckoutRequestID") @NotBlank String CheckoutRequestID
) {}
