package com.wwy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("test", "Hello Redis");
                String value = (String) redisTemplate.opsForValue().get("test");
                System.out.println("Redis test successful: " + value);
            } catch (Exception e) {
                System.out.println("Redis connection failed: " + e.getMessage());
            }
        } else {
            System.out.println("RedisTemplate is null - Redis configuration issue");
        }
    }
} 