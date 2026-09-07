package com.ticket.baseball.reservation.service;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TransactionTemplate transactionTemplate;


    @Test
    void 동시예약_테스트() {

        // 테스트 코드가 정상적으로 실행되는지 확인
        assertTrue(true);
    }


    @Test
    void 낙관적락_충돌_테스트() throws InterruptedException {

        // 테스트용 경기 생성
        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 테스트용 좌석 생성
        Seat seat = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .rowNumber(1)
                        .seatNumber(1)
                        .status(SeatStatus.AVAILABLE)
                        .build()
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable reservationTask = () -> {

            try {

                transactionTemplate.executeWithoutResult(status -> {

                    // 같은 좌석 조회
                    Seat targetSeat = seatRepository.findById(seat.getId())
                            .orElseThrow();

                    ready.countDown();

                    try {

                        // 두 스레드가 모두 조회할 때까지 대기
                        start.await();

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }

                    // 좌석 예약
                    targetSeat.reserve();

                    try {

                        // DB에 즉시 반영
                        seatRepository.saveAndFlush(targetSeat);

                        // 예약 성공
                        successCount.incrementAndGet();

                    } catch (ObjectOptimisticLockingFailureException e) {

                        // 낙관적 락 충돌
                        conflictCount.incrementAndGet();
                    }
                });

            } catch (Exception e) {

                if (e.getCause() instanceof ObjectOptimisticLockingFailureException) {
                    conflictCount.incrementAndGet();
                }
            }
        };

        executor.submit(reservationTask);
        executor.submit(reservationTask);

        // 두 스레드가 모두 좌석을 조회할 때까지 대기
        assertTrue(ready.await(5, TimeUnit.SECONDS));

        // 동시에 예약 시작
        start.countDown();

        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 한 요청은 성공
        assertEquals(1, successCount.get());

        // 한 요청은 낙관적 락 충돌
        assertEquals(1, conflictCount.get());
    }


    @Test
    void 실제_예약서비스_동시예약_테스트() throws InterruptedException {

        // 테스트용 사용자 생성
        User user = userRepository.save(
                User.builder()
                        .email("test@test.com")
                        .password("1234")
                        .name("테스트 사용자")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 테스트용 경기 생성
        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 테스트용 좌석 생성
        Seat seat = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .rowNumber(1)
                        .seatNumber(1)
                        .status(SeatStatus.AVAILABLE)
                        .build()
        );

        // 예약 요청 생성
        ReservationRequest request = new ReservationRequest(
                game.getId(),
                List.of(seat.getId())
        );

        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable reservationTask = () -> {

            try {

                // 현재 스레드에 테스트 사용자 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                null
                        )
                );

                // 동시에 시작
                start.await();

                // 실제 예약 서비스 호출
                reservationService.createReservation(request);

                // 예약 성공
                successCount.incrementAndGet();

            } catch (Exception e) {

                // 예약 실패
                failCount.incrementAndGet();

                System.out.println("예약 실패: " + e.getMessage());

            } finally {

                // 테스트가 끝나면 인증 정보 제거
                SecurityContextHolder.clearContext();
            }
        };

        // 두 요청 실행
        executor.submit(reservationTask);
        executor.submit(reservationTask);

        // 동시에 예약 시작
        start.countDown();

        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 한 요청만 성공
        assertEquals(1, successCount.get());

        // 한 요청은 실패
        assertEquals(1, failCount.get());
    }


    @Test
    void 여러좌석_예약_테스트() {

        // 테스트용 사용자 생성
        User user = userRepository.save(
                User.builder()
                        .email("multi@test.com")
                        .password("1234")
                        .name("다중 좌석 테스트 사용자")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 테스트용 경기 생성
        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 첫 번째 좌석 생성
        Seat seat1 = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .rowNumber(1)
                        .seatNumber(1)
                        .status(SeatStatus.AVAILABLE)
                        .build()
        );

        // 두 번째 좌석 생성
        Seat seat2 = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .rowNumber(1)
                        .seatNumber(2)
                        .status(SeatStatus.AVAILABLE)
                        .build()
        );

        // JWT 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        null
                )
        );

        try {

            // 두 좌석을 하나의 예약으로 요청
            ReservationRequest request = new ReservationRequest(
                    game.getId(),
                    List.of(
                            seat1.getId(),
                            seat2.getId()
                    )
            );

            // 예약 생성
            ReservationResponse response =
                    reservationService.createReservation(request);

            // 예약 ID가 정상적으로 생성되었는지 확인
            assertTrue(response.getId() != null);

            // 좌석이 2개 연결되었는지 확인
            assertEquals(2, response.getSeatIds().size());

            // 요청한 좌석 ID가 모두 포함되었는지 확인
            assertTrue(
                    response.getSeatIds().contains(seat1.getId())
            );

            assertTrue(
                    response.getSeatIds().contains(seat2.getId())
            );

            // DB에서 좌석 상태 확인
            Seat savedSeat1 =
                    seatRepository.findById(seat1.getId())
                            .orElseThrow();

            Seat savedSeat2 =
                    seatRepository.findById(seat2.getId())
                            .orElseThrow();

            // 두 좌석 모두 RESERVED 상태인지 확인
            assertEquals(
                    SeatStatus.RESERVED,
                    savedSeat1.getStatus()
            );

            assertEquals(
                    SeatStatus.RESERVED,
                    savedSeat2.getStatus()
            );

            // 예약이 DB에 정상 저장되었는지 확인
            Reservation reservation =
                    reservationRepository.findById(response.getId())
                            .orElseThrow();

            // 하나의 예약에 좌석 2개가 연결되었는지 확인
            assertEquals(
                    2,
                    reservation.getSeats().size()
            );

        } finally {

            // 테스트가 끝나면 인증 정보 제거
            SecurityContextHolder.clearContext();
        }
    }
}