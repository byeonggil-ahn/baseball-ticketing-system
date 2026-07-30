package com.ticket.baseball.seat.service;

import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// 좌석 비즈니스 로직 처리
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    // 전체 좌석 조회
    public List<Seat> getSeats() {
        return seatRepository.findAll();
    }

    // 좌석 저장
    public Seat saveSeat(Seat seat) {
        return seatRepository.save(seat);
    }
}