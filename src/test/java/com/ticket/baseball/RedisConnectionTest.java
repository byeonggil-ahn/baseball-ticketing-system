package com.ticket.baseball;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void redisConnectionTest() {

        // Redis에 테스트 데이터 저장
        redisTemplate.opsForValue().set("test", "hello");

        // Redis에서 테스트 데이터 조회
        String value = redisTemplate.opsForValue().get("test");

        System.out.println("Redis 테스트 결과 = " + value);
    }
}