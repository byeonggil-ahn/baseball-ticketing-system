package com.ticket.baseball.reservation.service;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import com.ticket.baseball.queue.QueueService;
import com.ticket.baseball.reservation.dto.ReservationRequest;
import com.ticket.baseball.reservation.dto.ReservationResponse;
import com.ticket.baseball.reservation.entity.Reservation;
import com.ticket.baseball.reservation.entity.ReservationSeat;
import com.ticket.baseball.reservation.entity.ReservationStatus;
import com.ticket.baseball.reservation.repository.ReservationRepository;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.service.SeatLockService;
import com.ticket.baseball.seat.service.SeatService;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SeatLockService seatLockService;
    private final SeatService seatService;
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

        // 현재 JWT로 로그인한 사용자의 로그인 아이디 가져오기
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 로그인 아이디로 사용자 조회
        User user = userRepository.findByLoginId(loginId)
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

        // 좌석 ID 목록 검증
        List<Long> seatIds = request.getSeatIds();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT
            );
        }

        // 동일한 좌석 ID가 여러 번 요청되었는지 확인
        if (seatIds.size() != seatIds.stream().distinct().count()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT
            );
        }

        // 좌석 Lock 획득 순서를 항상 동일하게 유지
        List<Long> sortedSeatIds = seatIds.stream()
                .sorted()
                .toList();

        // 좌석 조회
        List<Seat> seats =
                seatService.getSeatsByIds(sortedSeatIds);

        // 좌석이 해당 경기의 좌석인지 확인
        // 이미 예약된 좌석인지 확인
        seatService.validateSeats(
                seats,
                game.getId()
        );

        // Redis에서 여러 좌석을 순서대로 선점
        List<Long> lockedSeatIds = new ArrayList<>();

        try {

            for (Long seatId : sortedSeatIds) {

                boolean locked = seatLockService.lockSeat(
                        game.getId(),
                        seatId,
                        user.getId()
                );

                if (!locked) {

                    throw new BusinessException(
                            ErrorCode.DUPLICATE_RESERVATION
                    );
                }

                // 실제로 Lock을 획득한 좌석 기록
                lockedSeatIds.add(seatId);
            }

            // Redis Lock 획득 후 DB 상태 다시 확인
            seatService.validateSeats(
                    seats,
                    game.getId()
            );

            // 여러 좌석을 RESERVED 상태로 변경
            seatService.reserveSeats(seats);

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .game(game)
                    .build();

            // 예약 저장
            Reservation savedReservation =
                    reservationRepository.save(reservation);

            // 예약과 여러 좌석 연결
            for (Seat seat : seats) {

                ReservationSeat reservationSeat =
                        ReservationSeat.builder()
                                .reservation(savedReservation)
                                .seat(seat)
                                .build();

                savedReservation.addReservationSeat(
                        reservationSeat
                );
            }

            return toResponse(savedReservation);

        } catch (ObjectOptimisticLockingFailureException e) {

            // 낙관적 락 충돌 발생 시 획득한 Redis Lock 전부 해제
            unlockSeats(
                    game.getId(),
                    lockedSeatIds
            );

            throw new BusinessException(
                    ErrorCode.OPTIMISTIC_LOCK_CONFLICT
            );

        } catch (BusinessException e) {

            // 예약 실패 시 획득한 Redis Lock 전부 해제
            unlockSeats(
                    game.getId(),
                    lockedSeatIds
            );

            throw e;

        } catch (Exception e) {

            // 예상하지 못한 오류가 발생해도 Lock 정리
            unlockSeats(
                    game.getId(),
                    lockedSeatIds
            );

            throw e;
        }
    }

    // 예약 취소
    @Transactional
    public void cancelReservation(
            Long reservationId
    ) {

        // 현재 JWT로 로그인한 사용자의 로그인 아이디 가져오기
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 로그인 아이디로 현재 사용자 조회
        User user = userRepository.findByLoginId(loginId)
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

        // 예약에 연결된 모든 좌석 조회
        List<Seat> seats = reservation.getReservationSeats()
                .stream()
                .map(ReservationSeat::getSeat)
                .toList();

        // 모든 좌석을 AVAILABLE 상태로 복구
        seatService.cancelSeats(seats);

        // 모든 좌석의 Redis Lock 해제
        for (Seat seat : seats) {

            seatLockService.unlockSeat(
                    reservation.getGame().getId(),
                    seat.getId()
            );
        }
    }

    // 획득한 Redis Lock 전체 해제
    private void unlockSeats(
            Long gameId,
            List<Long> seatIds
    ) {

        for (Long seatId : seatIds) {

            seatLockService.unlockSeat(
                    gameId,
                    seatId
            );
        }
    }

    // Entity를 DTO로 변환
    private ReservationResponse toResponse(
            Reservation reservation
    ) {

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .gameId(reservation.getGame().getId())
                .seatIds(
                        reservation.getReservationSeats()
                                .stream()
                                .map(ReservationSeat::getSeat)
                                .map(Seat::getId)
                                .toList()
                )
                .reservedAt(reservation.getReservedAt())
                .build();
    }
}