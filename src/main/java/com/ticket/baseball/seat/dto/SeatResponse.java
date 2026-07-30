package com.ticket.baseball.seat.dto;

import com.ticket.baseball.seat.entity.Seat;
import lombok.Getter;

@Getter
public class SeatResponse {

    // 좌석 ID
    private final Long id;

    // 좌석 구역
    private final String section;

    // 좌석 행
    private final Integer rowNumber;

    // 좌석 번호
    private final Integer seatNumber;

    // 좌석 상태
    private final String status;


    public SeatResponse(Seat seat) {
        this.id = seat.getId();
        this.section = seat.getSection();
        this.rowNumber = seat.getRowNumber();
        this.seatNumber = seat.getSeatNumber();
        this.status = seat.getStatus().name();
    }
}