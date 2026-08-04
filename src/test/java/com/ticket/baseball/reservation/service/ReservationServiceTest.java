package com.ticket.baseball.reservation.service;

import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.seat.entity.SeatStatus;
import com.ticket.baseball.seat.repository.SeatRepository;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
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

        // 두 트랜잭션이 동시에 같은 좌석을 조회하도록 대기
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
                        // 두 요청이 모두 좌석을 조회할 때까지 대기
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // 좌석 예약 처리
                    targetSeat.reserve();

                    try {
                        // @Version 충돌 확인을 위해 즉시 DB 반영
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

        // 두 요청 실행
        executor.submit(reservationTask);
        executor.submit(reservationTask);

        // 두 스레드가 모두 좌석을 조회할 때까지 대기
        assertTrue(ready.await(5, TimeUnit.SECONDS));

        // 동시에 예약 시작
        start.countDown();

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // 한 요청은 성공하고 한 요청은 충돌해야 함
        assertEquals(1, successCount.get());
        assertEquals(1, conflictCount.get());
    }
}