package com.example.mpesa.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StkPushResponse(
        @JsonProperty("MerchantRequestID") String MerchantRequestID,
        @JsonProperty("CheckoutRequestID") String CheckoutRequestID,
        @JsonProperty("ResponseCode") String ResponseCode,
        @JsonProperty("ResponseDescription") String ResponseDescription,
        @JsonProperty("CustomerMessage") String CustomerMessage
) {}
