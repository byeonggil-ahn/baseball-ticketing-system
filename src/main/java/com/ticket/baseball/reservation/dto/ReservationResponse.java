package com.ticket.baseball.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReservationResponse {

    // 예약 정보
    private Long id;
    private Long userId;
    private Long gameId;

    // 예약한 좌석 ID 목록
    private List<Long> seatIds;

    private LocalDateTime reservedAt;
}