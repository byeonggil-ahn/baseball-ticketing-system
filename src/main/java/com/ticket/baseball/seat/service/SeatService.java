package com.ticket.baseball.seat.service;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.entity.SeatStatus;
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

    // 경기별 좌석 조회
    public List<Seat> getSeatsByGameId(Long gameId) {
        return seatRepository.findByGameId(gameId);
    }

    // 좌석 저장
    public Seat saveSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    // 여러 좌석 조회
    public List<Seat> getSeatsByIds(List<Long> seatIds) {

        List<Seat> seats = seatRepository.findAllById(seatIds);

        // 요청한 좌석 수와 조회된 좌석 수가 다르면 잘못된 좌석 요청
        if (seats.size() != seatIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return seats;
    }

    // 좌석 예약 가능 여부 확인
    public void validateSeats(
            List<Seat> seats,
            Long gameId
    ) {

        for (Seat seat : seats) {

            // 해당 경기의 좌석인지 확인
            if (!seat.getGame().getId().equals(gameId)) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT
                );
            }

            // 이미 예약된 좌석인지 확인
            if (seat.getStatus() == SeatStatus.RESERVED) {
                throw new BusinessException(
                        ErrorCode.DUPLICATE_RESERVATION
                );
            }
        }
    }

    // 여러 좌석 예약
    public void reserveSeats(List<Seat> seats) {

        for (Seat seat : seats) {
            seat.reserve();
        }

        seatRepository.saveAllAndFlush(seats);
    }

    // 여러 좌석 예약 취소
    public void cancelSeats(List<Seat> seats) {

        for (Seat seat : seats) {
            seat.cancelReservation();
        }

        seatRepository.saveAll(seats);
    }
}