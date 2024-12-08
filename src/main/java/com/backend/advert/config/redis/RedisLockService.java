package com.backend.advert.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate redisTemplate;

    public void lock(String key) {
        boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCK", Duration.ofSeconds(10));
        if (!acquired) {
            throw new RuntimeException("잠금을 획득할 수 없습니다.");
        }
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

}
