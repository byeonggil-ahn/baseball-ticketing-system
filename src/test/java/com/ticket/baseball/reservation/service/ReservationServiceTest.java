package com.ticket.baseball.reservation.service;

import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import com.ticket.baseball.queue.QueueService;
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

@SpringBootTest(properties = {
        "TOSS_CLIENT_KEY=test-client-key",
        "TOSS_SECRET_KEY=test-secret-key"
})
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
    private QueueService queueService;

    @Autowired
    private TransactionTemplate transactionTemplate;


    @Test
    void 낙관적락_충돌_테스트() throws InterruptedException {

        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .reservationStartAt(LocalDateTime.now().minusHours(1))
                        .reservationEndAt(LocalDateTime.now().plusHours(1))
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Seat seat = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .seatNumber(1)
                        .seatGrade("VIP")
                        .price(100000)
                        .status(SeatStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
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

                    Seat targetSeat = seatRepository.findById(seat.getId())
                            .orElseThrow();

                    ready.countDown();

                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    targetSeat.reserve();

                    try {
                        seatRepository.saveAndFlush(targetSeat);
                        successCount.incrementAndGet();

                    } catch (ObjectOptimisticLockingFailureException e) {
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

        assertTrue(ready.await(5, TimeUnit.SECONDS));

        start.countDown();

        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successCount.get());
        assertEquals(1, conflictCount.get());
    }


    @Test
    void 실제_예약서비스_동시예약_테스트() throws InterruptedException {

        String uniqueId = "concurrent" + System.nanoTime();

        User user = userRepository.save(
                User.builder()
                        .loginId(uniqueId)
                        .password("1234")
                        .nickname("동시 예약 테스트 사용자")
                        .email(uniqueId + "@test.com")
                        .role("USER")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .reservationStartAt(LocalDateTime.now().minusHours(1))
                        .reservationEndAt(LocalDateTime.now().plusHours(1))
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Seat seat = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .seatNumber(1)
                        .seatGrade("VIP")
                        .price(100000)
                        .status(SeatStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        ReservationRequest request = new ReservationRequest(
                game.getId(),
                List.of(seat.getId())
        );

        // 대기열 진입 후 통과
        queueService.enterQueue(game.getId(), user.getId());
        queueService.processQueue(game.getId());

        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable reservationTask = () -> {

            try {

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user.getLoginId(),
                                null,
                                null
                        )
                );

                start.await();

                reservationService.createReservation(request);

                successCount.incrementAndGet();

            } catch (Exception e) {

                failCount.incrementAndGet();

                System.out.println("예약 실패: " + e.getMessage());

            } finally {

                SecurityContextHolder.clearContext();
            }
        };

        executor.submit(reservationTask);
        executor.submit(reservationTask);

        start.countDown();

        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());
    }


    @Test
    void 여러좌석_예약_테스트() {

        String uniqueId = "multi" + System.nanoTime();

        User user = userRepository.save(
                User.builder()
                        .loginId(uniqueId)
                        .password("1234")
                        .nickname("다중 좌석 테스트 사용자")
                        .email(uniqueId + "@test.com")
                        .role("USER")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Game game = gameRepository.save(
                Game.builder()
                        .homeTeam("한화 이글스")
                        .awayTeam("LG 트윈스")
                        .stadium("대전 한화생명 볼파크")
                        .gameDate(LocalDateTime.now())
                        .reservationStartAt(LocalDateTime.now().minusHours(1))
                        .reservationEndAt(LocalDateTime.now().plusHours(1))
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Seat seat1 = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .seatNumber(1)
                        .seatGrade("VIP")
                        .price(100000)
                        .status(SeatStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Seat seat2 = seatRepository.save(
                Seat.builder()
                        .game(game)
                        .section("1루")
                        .seatNumber(2)
                        .seatGrade("VIP")
                        .price(100000)
                        .status(SeatStatus.AVAILABLE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 대기열 진입 후 통과
        queueService.enterQueue(game.getId(), user.getId());
        queueService.processQueue(game.getId());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getLoginId(),
                        null,
                        null
                )
        );

        try {

            ReservationRequest request = new ReservationRequest(
                    game.getId(),
                    List.of(
                            seat1.getId(),
                            seat2.getId()
                    )
            );

            ReservationResponse response =
                    reservationService.createReservation(request);

            assertTrue(response.getId() != null);

            assertEquals(
                    2,
                    response.getSeatIds().size()
            );

            assertTrue(
                    response.getSeatIds().contains(seat1.getId())
            );

            assertTrue(
                    response.getSeatIds().contains(seat2.getId())
            );

            Seat savedSeat1 =
                    seatRepository.findById(seat1.getId())
                            .orElseThrow();

            Seat savedSeat2 =
                    seatRepository.findById(seat2.getId())
                            .orElseThrow();

            assertEquals(
                    SeatStatus.RESERVED,
                    savedSeat1.getStatus()
            );

            assertEquals(
                    SeatStatus.RESERVED,
                    savedSeat2.getStatus()
            );

            int reservationSeatCount = transactionTemplate.execute(status -> {
                Reservation reservation =
                        reservationRepository.findById(response.getId())
                                .orElseThrow();

                return reservation.getReservationSeats().size();
            });

            assertEquals(2, reservationSeatCount);

        } finally {

            SecurityContextHolder.clearContext();
        }
    }
}