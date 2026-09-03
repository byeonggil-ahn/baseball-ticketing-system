package com.ticket.baseball.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentRequest {

    // 토스 결제 키
    private String paymentKey;

    // 토스 주문 ID
    private String orderId;

    // 결제 금액
    private Long amount;
}