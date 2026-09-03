package com.ticket.baseball.payment.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TossPaymentService {

    // 토스 결제 API 주소
    private static final String TOSS_PAYMENT_URL =
            "https://api.tosspayments.com/v1/payments/confirm";

    // 토스 Secret Key
    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final RestClient restClient;

    public TossPaymentService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    // 토스 결제 승인 요청
    public String confirmPayment(
            String paymentKey,
            String orderId,
            int amount) {

        // Secret Key를 Base64로 인코딩
        String encodedKey = Base64.getEncoder()
                .encodeToString(
                        (secretKey + ":")
                                .getBytes(StandardCharsets.UTF_8)
                );

        // 토스 결제 승인 API 호출
        return restClient.post()
                .uri(TOSS_PAYMENT_URL)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + encodedKey
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "paymentKey": "%s",
                            "orderId": "%s",
                            "amount": %d
                        }
                        """.formatted(paymentKey, orderId, amount))
                .retrieve()
                .body(String.class);
    }
}