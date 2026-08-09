package com.ticket.baseball;

import com.ticket.baseball.queue.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY = "queue:waiting";

    // 테스트 전에 기존 대기열 데이터 삭제
    @BeforeEach
    void setUp() {
        redisTemplate.delete(QUEUE_KEY);
    }

    @Test
    void 대기열_순번_테스트() {

        // 사용자 3명을 대기열에 등록
        queueService.enterQueue(101L);
        queueService.enterQueue(102L);
        queueService.enterQueue(103L);

        // 각 사용자의 대기 순번 확인
        Long position1 = queueService.getQueuePosition(101L);
        Long position2 = queueService.getQueuePosition(102L);
        Long position3 = queueService.getQueuePosition(103L);

        // 예상 순번과 실제 순번 비교
        assertEquals(1L, position1);
        assertEquals(2L, position2);
        assertEquals(3L, position3);
    }
}