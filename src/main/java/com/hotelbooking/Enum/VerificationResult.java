package com.hotelbooking.Enum;

import lombok.Getter;

@Getter
public class VerificationResult {
    private final String status;
    private final boolean isValid;

    private VerificationResult(String status, boolean isValid) {
        this.status = status;
        this.isValid = isValid;
    }

    public static VerificationResult valid() {
        return new VerificationResult("VALID", true);
    }
    public static VerificationResult invalid() {
        return new VerificationResult("INVALID", false);
    }
    public static VerificationResult expired() {
        return new VerificationResult("EXPIRED", false);
    }
    public static VerificationResult notFound() {
        return new VerificationResult("NOT_FOUND", false);
    }
}