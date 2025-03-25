package com.shop.shoppingevent.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventService {

    private final JdbcTemplate eventJdbcTemplate;

    public EventService(@Qualifier("eventJdbcTemplate") JdbcTemplate eventJdbcTemplate) {
        this.eventJdbcTemplate = eventJdbcTemplate;
    }

    @Transactional
    public void saveParticipation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        boolean given = false;
        String sql = "INSERT INTO participation (user_id, create_datetime, given) VALUES (?, ?, ?)";
        eventJdbcTemplate.update(sql, userId, now, given);
    }

    public int countParticipation() {
        // TODO 1. Redis를 이용하여 참여자 수 조회
        return 0;
    }
}
