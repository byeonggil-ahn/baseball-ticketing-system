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
        // 현재는 테스트를 위해 사용자 ID를 임시로 사용
        return reservationService.createReservation(request, 3126L);
    }
}