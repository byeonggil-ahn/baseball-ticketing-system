package com.ticket.baseball.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedisTemplate<String, String> redisTemplate;

    // 좌석 선점 유지 시간: 5분
    private static final long LOCK_TIME = 5;

    // 좌석 선점
    public boolean lockSeat(Long gameId, Long seatId, Long userId) {

        String key = "seat:" + gameId + ":" + seatId;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        userId.toString(),
                        LOCK_TIME,
                        TimeUnit.MINUTES
                );

        return Boolean.TRUE.equals(success);
    }

    // 좌석 선점 해제
    public void unlockSeat(Long gameId, Long seatId) {

        String key = "seat:" + gameId + ":" + seatId;

        redisTemplate.delete(key);
    }
}