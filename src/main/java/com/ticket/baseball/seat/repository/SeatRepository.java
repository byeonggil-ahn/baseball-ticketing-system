package com.ticket.baseball.seat.repository;

import com.ticket.baseball.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 좌석 데이터 접근 Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 경기별 좌석 조회
    List<Seat> findByGameId(Long gameId);
}