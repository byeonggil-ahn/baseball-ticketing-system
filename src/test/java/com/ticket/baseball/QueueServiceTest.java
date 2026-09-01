package com.ticket.baseball;

import com.ticket.baseball.queue.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Long GAME_ID = 1L;

    private static final String QUEUE_KEY =
            "queue:waiting:" + GAME_ID;

    private static final String PASSED_QUEUE_KEY =
            "queue:passed:" + GAME_ID;

    // 테스트 전에 기존 대기열 및 통과 데이터 삭제
    @BeforeEach
    void setUp() {
        redisTemplate.delete(QUEUE_KEY);
        redisTemplate.delete(PASSED_QUEUE_KEY);
    }

    @Test
    void 대기열_순번_테스트() {

        // 사용자 3명을 대기열에 등록
        queueService.enterQueue(GAME_ID, 101L);
        queueService.enterQueue(GAME_ID, 102L);
        queueService.enterQueue(GAME_ID, 103L);

        // 각 사용자의 대기 순번 확인
        Long position1 =
                queueService.getQueuePosition(GAME_ID, 101L);

        Long position2 =
                queueService.getQueuePosition(GAME_ID, 102L);

        Long position3 =
                queueService.getQueuePosition(GAME_ID, 103L);

        // 예상 순번과 실제 순번 비교
        assertEquals(1L, position1);
        assertEquals(2L, position2);
        assertEquals(3L, position3);
    }

    @Test
    void 대기열_통과_테스트() {

        // 사용자 3명을 대기열에 등록
        queueService.enterQueue(GAME_ID, 101L);
        queueService.enterQueue(GAME_ID, 102L);
        queueService.enterQueue(GAME_ID, 103L);

        // 아직 통과하지 않았는지 확인
        assertFalse(
                queueService.isQueuePassed(GAME_ID, 101L)
        );

        // 첫 번째 사용자 통과 처리
        String passedUserId =
                queueService.processQueue(GAME_ID);

        // 통과한 사용자가 101번인지 확인
        assertEquals("101", passedUserId);

        // 101번 사용자가 통과 상태인지 확인
        assertTrue(
                queueService.isQueuePassed(GAME_ID, 101L)
        );

        // 102번 사용자는 아직 통과하지 않았는지 확인
        assertFalse(
                queueService.isQueuePassed(GAME_ID, 102L)
        );

        // 통과한 101번 사용자는 대기열에서 제거됐는지 확인
        assertEquals(
                0L,
                queueService.getQueuePosition(GAME_ID, 101L)
        );

        // 102번 사용자가 대기열 첫 번째가 됐는지 확인
        assertEquals(
                1L,
                queueService.getQueuePosition(GAME_ID, 102L)
        );
    }

    @Test
    void 대기열이_비어있으면_통과처리하지않음() {

        // 대기열이 비어있는 상태에서 처리
        String result =
                queueService.processQueue(GAME_ID);

        // 통과할 사용자가 없으므로 null
        assertNull(result);
    }
}