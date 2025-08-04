package com.hotelbooking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GenerateQrResponse {

    // Document Reference: https://developer.payway.com.kh/qr-api-14530840e0

    private String qrString;
    private String qrImage;
    private String abapay_deeplink;
    private String app_store;
    private String play_store;
    private double amount;
    private String currency;
    private Status status;

    @Getter @Setter
    public static class Status {
        private String code;
        private String message;
        private String lang;
        private String tran_id;
        private String trace_id;
    }

}