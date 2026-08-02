package com.ticket.baseball.reservation.controller;

import com.ticket.baseball.reservation.entity.Reservation;
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
    public List<Reservation> getReservations() {
        return reservationService.getReservations();
    }
}