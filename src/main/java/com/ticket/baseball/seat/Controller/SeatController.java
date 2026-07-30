package com.ticket.baseball.seat.controller;

import com.ticket.baseball.seat.dto.SeatResponse;
import com.ticket.baseball.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 좌석 API Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/seats")
public class SeatController {

    private final SeatService seatService;

    // 전체 좌석 조회
    @GetMapping
    public List<SeatResponse> getSeats() {

        return seatService.getSeats()
                .stream()
                .map(SeatResponse::new)
                .toList();
    }
}