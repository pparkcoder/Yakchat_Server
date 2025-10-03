package com.kaidey.yakchatproject.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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

    public void setDataExpire(String key, String value, long timeoutSeconds) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    public long getExpire(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl == null ? -1L : ttl;
    }

    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 키 존재 여부 확인
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis 키 존재 확인 실패 - key: {}", key, e);
            return false;
        }
    }
}
