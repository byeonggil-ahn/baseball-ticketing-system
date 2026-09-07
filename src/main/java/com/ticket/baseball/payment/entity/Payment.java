package com.ticket.baseball.payment.entity;

import com.ticket.baseball.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제한 예약
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // 결제 금액
    @Column(nullable = false)
    private Long amount;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 결제 시간
    private LocalDateTime paidAt;

    // 토스 결제 키
    @Column(nullable = false)
    private String paymentKey;

    // 토스 주문 ID
    @Column(nullable = false)
    private String orderId;

    @Builder
    public Payment(
            Reservation reservation,
            Long amount,
            String paymentKey,
            String orderId
    ) {
        this.reservation = reservation;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 취소
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}