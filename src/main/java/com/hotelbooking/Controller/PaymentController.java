package com.hotelbooking.Controller;
;
import com.hotelbooking.dto.CallbackRequest;
import com.hotelbooking.service.ABAPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/aba/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all origins (replace "*" with frontend URL in production)
public class PaymentController {

    private final ABAPayService abaPayService;

    @GetMapping("generate-qr-image")
    ResponseEntity<byte[]> generateQrImage(@RequestParam @Valid double amount, @RequestParam @Valid String ccy, @RequestParam @Valid String txnId) {
        return abaPayService.qrImage(amount, ccy, txnId);
    }

    @PostMapping("callback")
    void ExCallbackRequest(@RequestBody CallbackRequest request) {
        abaPayService.txnCallback(request);
    }

}