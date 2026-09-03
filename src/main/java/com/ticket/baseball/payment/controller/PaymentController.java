package com.ticket.baseball.payment.controller;

import com.ticket.baseball.payment.dto.PaymentResponse;
import com.ticket.baseball.payment.dto.TossPaymentRequest;
import com.ticket.baseball.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 토스 결제 승인
    @PostMapping("/{reservationId}/confirm")
    public PaymentResponse confirmPayment(
            @PathVariable Long reservationId,
            @RequestBody TossPaymentRequest request
    ) {
        return paymentService.confirmPayment(
                reservationId,
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );
    }

    // 결제 조회
    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(
            @PathVariable Long paymentId
    ) {
        return paymentService.getPayment(paymentId);
    }
}