package com.shop.shoppingevent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EventService {

    private final JdbcTemplate eventJdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public EventService(@Qualifier("eventJdbcTemplate") JdbcTemplate eventJdbcTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.eventJdbcTemplate = eventJdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void saveParticipation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        boolean given = false;
        String sql = "INSERT INTO participation (user_id, create_datetime, given) VALUES (?, ?, ?)";
        eventJdbcTemplate.update(sql, userId, now, given);
    }

    public int countParticipation() {
        Object value = redisTemplate.opsForValue().get("event:count");
        if (value instanceof Number) {
            return (Integer) value;
        }
        log.error("Invalid value type: {} (Cant convert to Integer Type)", value);
        throw new IllegalStateException("Invalid value type");
    }
}
