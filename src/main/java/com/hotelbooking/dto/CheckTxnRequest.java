package com.hotelbooking.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckTxnRequest {

    // Document Reference: https://developer.payway.com.kh/check-transaction-14530826e0

    String req_time;
    String merchant_id;
    String tran_id;
    String hash;

}