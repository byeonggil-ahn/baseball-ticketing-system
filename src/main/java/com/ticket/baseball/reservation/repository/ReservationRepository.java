package com.ticket.baseball.reservation.repository;

import com.ticket.baseball.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

// 예약 DB 접근
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}