package com.ticket.baseball.seat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeatLockServiceTest {

    @Autowired
    private SeatLockService seatLockService;

    @Test
    void 좌석_선점에_성공한다() {
        // 테스트 준비
        Long gameId = 1L;
        Long seatId = 1L;
        Long userId = 100L;

        // 좌석 선점
        boolean result = seatLockService.lockSeat(gameId, seatId, userId);

        // 결과 확인
        assertTrue(result);

        // 테스트 후 좌석 선점 해제
        seatLockService.unlockSeat(gameId, seatId);
    }

    @Test
    void 이미_선점된_좌석은_다른_사용자가_선점할_수_없다() {
        // 테스트 준비
        Long gameId = 1L;
        Long seatId = 2L;
        Long firstUserId = 100L;
        Long secondUserId = 200L;

        // 첫 번째 사용자가 좌석 선점
        boolean firstResult =
                seatLockService.lockSeat(gameId, seatId, firstUserId);

        // 두 번째 사용자가 같은 좌석 선점 시도
        boolean secondResult =
                seatLockService.lockSeat(gameId, seatId, secondUserId);

        // 첫 번째 사용자는 선점에 성공해야 한다
        assertTrue(firstResult);

        // 두 번째 사용자는 선점에 실패해야 한다
        assertFalse(secondResult);

        // 테스트 후 좌석 선점 해제
        seatLockService.unlockSeat(gameId, seatId);
    }

    @Test
    void 좌석_선점_해제_후_다시_선점할_수_있다() {
        // 테스트 준비
        Long gameId = 1L;
        Long seatId = 3L;
        Long firstUserId = 100L;
        Long secondUserId = 200L;

        // 첫 번째 사용자가 좌석 선점
        boolean firstResult =
                seatLockService.lockSeat(gameId, seatId, firstUserId);

        // 첫 번째 사용자의 좌석 선점 해제
        seatLockService.unlockSeat(gameId, seatId);

        // 두 번째 사용자가 같은 좌석 선점 시도
        boolean secondResult =
                seatLockService.lockSeat(gameId, seatId, secondUserId);

        // 첫 번째 사용자는 선점에 성공해야 한다
        assertTrue(firstResult);

        // 선점이 해제되었으므로 두 번째 사용자도 선점에 성공해야 한다
        assertTrue(secondResult);

        // 테스트 후 좌석 선점 해제
        seatLockService.unlockSeat(gameId, seatId);
    }
}