package com.kaidey.yakchatproject.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${spring.data.redis.duration}")
    private int duration;

    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void setData(String key, String value) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Duration expiredTime = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expiredTime);
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
