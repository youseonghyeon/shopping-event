package com.shop.shoppingevent.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventService {

    private final JdbcTemplate jdbcTemplate;

    public EventService(@Qualifier("eventJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void saveParticipation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        boolean given = false;
        String sql = "INSERT INTO participation (user_id, create_datetime, given) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, now, given);
    }
}
