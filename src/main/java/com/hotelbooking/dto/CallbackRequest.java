package com.hotelbooking.dto;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CallbackRequest {

    // Document Reference: https://developer.payway.com.kh/folder-3158158
    // "Handle Callback URL for payment status updates"

    String tran_id;
    int apv;
    String status;
    String merchant_ref_no;

}