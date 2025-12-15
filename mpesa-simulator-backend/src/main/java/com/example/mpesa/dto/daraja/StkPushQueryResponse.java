package com.example.mpesa.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StkPushQueryResponse(
        @JsonProperty("ResponseCode") String ResponseCode,
        @JsonProperty("ResponseDescription") String ResponseDescription,
        @JsonProperty("MerchantRequestID") String MerchantRequestID,
        @JsonProperty("CheckoutRequestID") String CheckoutRequestID,
        @JsonProperty("ResultCode") String ResultCode,
        @JsonProperty("ResultDesc") String ResultDesc
) {}
