package com.shop.shoppingevent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void saveParticipation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        boolean given = false;
        String sql = "INSERT INTO participation (user_id, create_datetime, given) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, now, given);
    }
}
