package com.ticket.baseball.reservation.service;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import com.ticket.baseball.queue.QueueService;
import com.ticket.baseball.reservation.dto.ReservationRequest;
import com.ticket.baseball.reservation.dto.ReservationResponse;
import com.ticket.baseball.reservation.entity.Reservation;
import com.ticket.baseball.reservation.entity.ReservationStatus;
import com.ticket.baseball.reservation.repository.ReservationRepository;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.entity.SeatStatus;
import com.ticket.baseball.seat.repository.SeatRepository;
import com.ticket.baseball.seat.service.SeatLockService;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SeatLockService seatLockService;
    private final QueueService queueService;

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
            ReservationRequest request
    ) {

        // 현재 JWT로 로그인한 사용자의 이메일 가져오기
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        // 경기 조회
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        // 해당 경기의 대기열 통과 여부 확인
        if (!queueService.isQueuePassed(
                game.getId(),
                user.getId()
        )) {
            throw new BusinessException(
                    ErrorCode.QUEUE_NOT_PASSED
            );
        }

        // 좌석 조회
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        // 좌석이 해당 경기의 좌석인지 확인
        if (!seat.getGame().getId().equals(game.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // Redis에서 좌석을 먼저 선점
        boolean locked = seatLockService.lockSeat(
                request.getGameId(),
                request.getSeatId(),
                user.getId()
        );

        if (!locked) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_RESERVATION
            );
        }

        // DB에서 이미 예약된 좌석인지 확인
        if (seat.getStatus() == SeatStatus.RESERVED) {

            seatLockService.unlockSeat(
                    request.getGameId(),
                    request.getSeatId()
            );

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

            seatLockService.unlockSeat(
                    request.getGameId(),
                    request.getSeatId()
            );

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

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return toResponse(savedReservation);
    }

    // 예약 취소
    @Transactional
    public void cancelReservation(
            Long reservationId
    ) {

        // 현재 JWT로 로그인한 사용자의 이메일 가져오기
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 이메일로 현재 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        // 예약 조회
        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RESERVATION_NOT_FOUND
                                )
                        );

        // 예약한 사용자 본인인지 확인
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.RESERVATION_ACCESS_DENIED
            );
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.RESERVATION_ALREADY_CANCELLED
            );
        }

        // 예약 상태를 CANCELLED로 변경
        reservation.cancel();

        // 좌석 상태를 AVAILABLE로 변경
        Seat seat = reservation.getSeat();
        seat.cancelReservation();

        // Redis 좌석 선점 해제
        seatLockService.unlockSeat(
                reservation.getGame().getId(),
                seat.getId()
        );
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