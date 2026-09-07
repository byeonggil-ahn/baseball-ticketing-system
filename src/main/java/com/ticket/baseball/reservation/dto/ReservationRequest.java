package com.ticket.baseball.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    // 경기 ID
    private Long gameId;

    // 예약할 좌석 ID 목록
    private List<Long> seatIds;

    public ReservationRequest(
            Long gameId,
            List<Long> seatIds
    ) {
        this.gameId = gameId;
        this.seatIds = seatIds;
    }
}