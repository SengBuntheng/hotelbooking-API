package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    @JsonProperty("checkout_url")
    private String checkoutUrl;

    @JsonProperty("status")
    private Status status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String code;
        private String message;
        @JsonProperty("tran_id")
        private String tranId;
        @JsonProperty("trace_id")
        private String traceId;
    }
}
