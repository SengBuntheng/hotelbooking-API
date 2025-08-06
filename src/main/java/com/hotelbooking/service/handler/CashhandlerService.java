package com.hotelbooking.service.handler;
import com.hotelbooking.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
@Slf4j
public class CashhandlerService {
    private final RestTemplate restTemplate;

    public CashhandlerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String processCashPayment(PaymentRequest paymentRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + "your_api_key");

            HttpEntity<PaymentRequest> request = new HttpEntity<>(paymentRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.cashpayment.com/process",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("Response from Cash API: {}", response.getBody());
            return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
        } catch (Exception e) {
            log.error("Error processing cash payment", e);
            return null;
        }
    }
}