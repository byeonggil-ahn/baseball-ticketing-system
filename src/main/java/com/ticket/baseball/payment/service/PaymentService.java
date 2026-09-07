package com.ticket.baseball.payment.service;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.payment.dto.PaymentResponse;
import com.ticket.baseball.payment.entity.Payment;
import com.ticket.baseball.payment.repository.PaymentRepository;
import com.ticket.baseball.reservation.entity.Reservation;
import com.ticket.baseball.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentService tossPaymentService;

    // 결제 생성
    @Transactional
    public PaymentResponse createPayment(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loginId = authentication.getName();

        // 본인 예약 확인
        if (!reservation.getUser().getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        // 중복 결제 확인
        paymentRepository.findByReservationId(reservationId)
                .ifPresent(payment -> {
                    throw new BusinessException(
                            ErrorCode.PAYMENT_ALREADY_COMPLETED);
                });

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(10000L)
                .paymentKey("TEMP")
                .orderId("TEMP-" + reservationId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);
    }

    // 토스 결제 승인
    @Transactional
    public PaymentResponse confirmPayment(
            Long reservationId,
            String paymentKey,
            String orderId,
            Long amount
    ) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 중복 결제 확인
        paymentRepository.findByReservationId(reservationId)
                .ifPresent(payment -> {
                    throw new BusinessException(
                            ErrorCode.PAYMENT_ALREADY_COMPLETED);
                });

        // 결제 금액 확인
        if (amount != 10000L) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 토스 결제 승인 요청
        tossPaymentService.confirmPayment(
                paymentKey,
                orderId,
                amount.intValue()
        );

        // 토스 승인 성공 후 결제 저장
        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .paymentKey(paymentKey)
                .orderId(orderId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);
    }

    // 결제 조회
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loginId = authentication.getName();

        // 본인 결제 확인
        if (!payment.getReservation().getUser().getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        return new PaymentResponse(payment);
    }
}