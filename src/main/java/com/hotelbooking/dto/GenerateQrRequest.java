package com.hotelbooking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateQrRequest {

    // Class Reference: https://developer.payway.com.kh/qr-api-14530840e0

    private double amount;
    private String callback_url;
    private String currency;
    private String custom_fields;
    private String email;
    private String first_name;
    private String hash;
    private String items;
    private String last_name;
    private long lifetime;
    private String merchant_id;
    private String payment_option;
    private String payout;
    private String phone;
    private String purchase_type;
    private String qr_image_template;
    private String req_time;
    private String return_deeplink;
    private String return_params;
    private String tran_id;
}