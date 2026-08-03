package com.ticket.baseball.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    // 경기 ID
    private Long gameId;

    // 좌석 ID
    private Long seatId;
}