package com.hotelbooking.Enum;

public class VerificationResult {

    private final VerificationStatus status;

    private VerificationResult(VerificationStatus status) {
        this.status = status;
    }

    public static VerificationResult valid() {
        return new VerificationResult(VerificationStatus.VALID);
    }

    public static VerificationResult invalid() {
        return new VerificationResult(VerificationStatus.INVALID);
    }

    public static VerificationResult expired() {
        return new VerificationResult(VerificationStatus.EXPIRED);
    }

    public static VerificationResult notFound() {
        return new VerificationResult(VerificationStatus.NOT_FOUND);
    }

    public VerificationStatus getStatus() {
        return status;
    }
}
