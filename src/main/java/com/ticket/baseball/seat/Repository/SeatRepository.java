package com.ticket.baseball.seat.repository;

import com.ticket.baseball.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

// 좌석 데이터 접근 Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

}