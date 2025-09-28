package com.hotelbooking.Enum;
public enum PaymentStatusCode {
    SUCCESS(0),
    PENDING(2),
    FAILED(1);

    private final int code;

    PaymentStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PaymentStatusCode fromCode(int code) {
        for (PaymentStatusCode status : values()) {
            if (status.code == code) return status;
        }
        return FAILED;
    }
}