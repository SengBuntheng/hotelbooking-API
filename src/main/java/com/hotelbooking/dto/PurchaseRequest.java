package com.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseRequest {

    @JsonProperty("req_time")
    private String reqTime;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("payment_option")
    private String paymentOption;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("tran_id")
    private String tranId;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("items")
    private String items;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    @JsonProperty("continue_success_url")
    private String continueSuccessUrl;

    @JsonProperty("custom_fields")
    private String customFields;
}
