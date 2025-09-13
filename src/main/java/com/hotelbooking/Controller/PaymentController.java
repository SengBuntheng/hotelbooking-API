package com.hotelbooking.Controller;

import com.hotelbooking.dto.CallbackRequest;
import com.hotelbooking.service.ABAPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/aba/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final ABAPayService abaPayService;

    @GetMapping("generate-qr-image")
    public ResponseEntity<byte[]> generateQrImage(
            @RequestParam double amount,
            @RequestParam String ccy,
            @RequestParam String txnId
    ) {
        ResponseEntity<byte[]> response = abaPayService.qrImage(amount, ccy, txnId);
        return response != null ? response : ResponseEntity.internalServerError().build();
    }

    @PostMapping("callback")
    public ResponseEntity<Void> ExCallbackRequest(@RequestBody CallbackRequest request) {
        abaPayService.txnCallback(request);
        return ResponseEntity.ok().build();
    }
}
