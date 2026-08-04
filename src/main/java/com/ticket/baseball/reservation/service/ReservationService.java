package com.ticket.baseball.reservation.service;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import com.ticket.baseball.reservation.dto.ReservationRequest;
import com.ticket.baseball.reservation.dto.ReservationResponse;
import com.ticket.baseball.reservation.entity.Reservation;
import com.ticket.baseball.reservation.repository.ReservationRepository;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.entity.SeatStatus;
import com.ticket.baseball.seat.repository.SeatRepository;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GameRepository gameRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    // 예약 전체 조회
    public List<ReservationResponse> getReservations() {

        return reservationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 예약 생성
    @Transactional
    public ReservationResponse createReservation(
            ReservationRequest request,
            Long userId
    ) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        // 경기 조회
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        // 좌석 조회
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        // 좌석이 해당 경기의 좌석인지 확인
        if (!seat.getGame().getId().equals(game.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 이미 예약된 좌석인지 확인
        if (seat.getStatus() == SeatStatus.RESERVED) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_RESERVATION
            );
        }

        // 좌석 상태를 RESERVED로 변경
        seat.reserve();

        // 낙관적 락 충돌 처리
        try {
            seatRepository.saveAndFlush(seat);

        } catch (ObjectOptimisticLockingFailureException e) {

            // 다른 사용자가 먼저 예약한 경우
            throw new BusinessException(
                    ErrorCode.OPTIMISTIC_LOCK_CONFLICT
            );
        }

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .game(game)
                .seat(seat)
                .build();

        // 예약 저장
        Reservation savedReservation =
                reservationRepository.save(reservation);

        return toResponse(savedReservation);
    }

    // Entity를 DTO로 변환
    private ReservationResponse toResponse(
            Reservation reservation
    ) {

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .gameId(reservation.getGame().getId())
                .seatId(reservation.getSeat().getId())
                .reservedAt(reservation.getReservedAt())
                .build();
    }
}