package com.ticket.baseball.seat.dto;

import com.ticket.baseball.seat.entity.Seat;
import lombok.Getter;

@Getter
public class SeatResponse {

    // 좌석 ID
    private final Long id;

    // 좌석 구역
    private final String section;

    // 좌석 번호
    private final Integer seatNumber;

    // 좌석 등급
    private final String seatGrade;

    // 좌석 가격
    private final Integer price;

    // 좌석 상태
    private final String status;

    public SeatResponse(Seat seat) {
        this.id = seat.getId();
        this.section = seat.getSection();
        this.seatNumber = seat.getSeatNumber();
        this.seatGrade = seat.getSeatGrade();
        this.price = seat.getPrice();
        this.status = seat.getStatus().name();
    }
}