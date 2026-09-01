package com.ticket.baseball.payment.controller;

import com.ticket.baseball.payment.dto.PaymentResponse;
import com.ticket.baseball.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{reservationId}")
    public PaymentResponse createPayment(
            @PathVariable Long reservationId
    ) {
        return paymentService.createPayment(reservationId);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(
            @PathVariable Long paymentId
    ) {
        return paymentService.getPayment(paymentId);
    }
}