package com.shop.shoppingevent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class EventServiceTest {

    @Mock
    private JdbcTemplate eventJdbcTemplate;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveParticipation() {
        Long userId = 123L;
        // saveParticipation 호출 시 JdbcTemplate.update가 올바른 인자로 호출되는지 검증
        eventService.saveParticipation(userId);

        String expectedSql = "INSERT INTO participation (user_id, create_datetime, given) VALUES (?, ?, ?)";
        // LocalDateTime은 동적으로 생성되므로 any(LocalDateTime.class)를 사용하고,
        // given은 false로 전달되어야 함.
        verify(eventJdbcTemplate).update(eq(expectedSql), eq(userId), any(), eq(false));
    }

    @Test
    void testCountParticipation() {
        // 현재 미구현 상태이므로 0을 반환해야 함.
        int count = eventService.countParticipation();
        assertEquals(0, count);
    }
}
