package com.ticket.baseball.reservation.controller;

import com.ticket.baseball.reservation.dto.ReservationRequest;
import com.ticket.baseball.reservation.dto.ReservationResponse;
import com.ticket.baseball.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 전체 조회
    @GetMapping
    public List<ReservationResponse> getReservations() {
        return reservationService.getReservations();
    }

    // 예약 생성
    @PostMapping
    public ReservationResponse createReservation(
            @RequestBody ReservationRequest request
    ) {
        return reservationService.createReservation(request, 3126L);    }
}