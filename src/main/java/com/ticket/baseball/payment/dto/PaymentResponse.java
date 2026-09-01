package com.ticket.baseball.payment.dto;

import com.ticket.baseball.payment.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponse {

    private final Long id;
    private final Long reservationId;
    private final Long amount;
    private final String status;
    private final LocalDateTime paidAt;

    // 결제 정보 반환
    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.reservationId = payment.getReservation().getId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus().name();
        this.paidAt = payment.getPaidAt();
    }
}