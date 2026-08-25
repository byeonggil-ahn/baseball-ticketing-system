package com.ticket.baseball.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    private Long gameId;

    private Long seatId;

    public ReservationRequest(Long gameId, Long seatId) {
        this.gameId = gameId;
        this.seatId = seatId;
    }
}