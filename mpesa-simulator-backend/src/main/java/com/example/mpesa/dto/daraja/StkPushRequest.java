package com.example.mpesa.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StkPushRequest(
        @JsonProperty("BusinessShortCode") @NotBlank String BusinessShortCode,
        @JsonProperty("Password") @NotBlank String Password,
        @JsonProperty("Timestamp") @NotBlank String Timestamp,
        @JsonProperty("TransactionType") @NotBlank String TransactionType,
        @JsonProperty("Amount") @NotNull BigDecimal Amount,
        @JsonProperty("PartyA") @NotBlank String PartyA,
        @JsonProperty("PartyB") @NotBlank String PartyB,
        @JsonProperty("PhoneNumber") @NotBlank String PhoneNumber,
        @JsonProperty("CallBackURL") String CallBackURL,
        @JsonProperty("AccountReference") String AccountReference,
        @JsonProperty("TransactionDesc") String TransactionDesc
) {}
