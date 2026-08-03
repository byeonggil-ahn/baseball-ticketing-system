package com.ticket.baseball.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    // 예약 정보
    private Long id;
    private Long userId;
    private Long gameId;
    private Long seatId;
    private LocalDateTime reservedAt;
}