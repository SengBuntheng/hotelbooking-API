package com.hotelbooking.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbaQrResponse {
    private String transactionId;
    private String qrCodeBase64;
    private String message;
    private boolean success;


}
