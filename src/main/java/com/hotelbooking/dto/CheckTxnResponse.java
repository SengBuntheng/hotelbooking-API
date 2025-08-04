package com.hotelbooking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckTxnResponse {

    // Document Reference: https://developer.payway.com.kh/check-transaction-14530826e0

    TxnData data;

    TxnStatus status;

    @Getter @Setter
    public static class TxnData {
        int payment_status_code;
        double total_amount;
        double original_amount;
        double refund_amount;
        double discount_amount;
        double payment_amount;
        String payment_currency;
        String apv;
        String payment_status;
        String transaction_date;
    }

    @Getter @Setter
    public static class TxnStatus {
        String code;
        String message;
        String tran_id;
    }

}