package com.ticket.baseball.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class QueueService {

    private static final String QUEUE_KEY_PREFIX = "queue:waiting:";
    private static final String PASSED_QUEUE_KEY_PREFIX = "queue:passed:";

    private final RedisTemplate<String, String> redisTemplate;

    // 경기별 대기열 Redis Key
    private String getQueueKey(Long gameId) {
        return QUEUE_KEY_PREFIX + gameId;
    }

    // 경기별 통과 사용자 Redis Key
    private String getPassedQueueKey(Long gameId) {
        return PASSED_QUEUE_KEY_PREFIX + gameId;
    }

    // 대기열에 사용자 추가
    public void enterQueue(Long gameId, Long userId) {

        double score = System.currentTimeMillis();

        redisTemplate.opsForZSet()
                .add(
                        getQueueKey(gameId),
                        userId.toString(),
                        score
                );
    }

    // 대기열 순번 조회
    public Long getQueuePosition(Long gameId, Long userId) {

        Long rank = redisTemplate.opsForZSet()
                .rank(
                        getQueueKey(gameId),
                        userId.toString()
                );

        // 대기열에 없으면 0 반환
        return rank == null ? 0L : rank + 1;
    }

    // 대기열에서 사용자 제거
    public void removeFromQueue(Long gameId, Long userId) {

        redisTemplate.opsForZSet()
                .remove(
                        getQueueKey(gameId),
                        userId.toString()
                );
    }

    // 해당 경기의 대기열 첫 번째 사용자 처리
    public String processQueue(Long gameId) {

        // 대기열의 가장 앞 사용자 조회
        Set<String> users = redisTemplate.opsForZSet()
                .range(
                        getQueueKey(gameId),
                        0,
                        0
                );

        // 대기열이 비어있는 경우
        if (users == null || users.isEmpty()) {
            return null;
        }

        // 첫 번째 사용자
        String userId = users.iterator().next();

        // 대기열에서 제거
        redisTemplate.opsForZSet()
                .remove(
                        getQueueKey(gameId),
                        userId
                );

        // 해당 경기의 대기열 통과 사용자로 등록
        redisTemplate.opsForSet()
                .add(
                        getPassedQueueKey(gameId),
                        userId
                );

        return userId;
    }

    // 해당 경기의 대기열 통과 여부 확인
    public boolean isQueuePassed(Long gameId, Long userId) {

        Boolean passed = redisTemplate.opsForSet()
                .isMember(
                        getPassedQueueKey(gameId),
                        userId.toString()
                );

        return Boolean.TRUE.equals(passed);
    }
}