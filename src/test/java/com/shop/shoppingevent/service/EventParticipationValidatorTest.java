package com.shop.shoppingevent.service;

import com.shop.shoppingevent.dto.EventJoinResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;

import static com.shop.shoppingevent.dto.EventJoinResult.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventParticipationValidatorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private EventParticipationValidator participationValidator;

    @Test
    void testTryParticipate_Success() {
        // userId "123"와 maxCount 1000으로 호출될 때 SUCCESS (1L)를 반환하도록 stubbing
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Arrays.asList("event:participants", "event:count")),
                eq("123"),
                eq(1000)
        )).thenReturn(1L);

        EventJoinResult result = participationValidator.tryParticipate(123L, 1000);
        assertEquals(SUCCESS, result);
    }

    @Test
    void testTryParticipate_AlreadyJoined() {
        // 이미 참여한 경우: 결과가 0L를 반환하도록 stubbing
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Arrays.asList("event:participants", "event:count")),
                eq("123"),
                eq(1000)
        )).thenReturn(0L);

        EventJoinResult result = participationValidator.tryParticipate(123L, 1000);
        assertEquals(EventJoinResult.ALREADY_JOINED, result);
    }

    @Test
    void testTryParticipate_SoldOut() {
        // 티켓 매진된 경우: 결과가 -1L를 반환하도록 stubbing
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Arrays.asList("event:participants", "event:count")),
                eq("123"),
                eq(1000)
        )).thenReturn(-1L);

        EventJoinResult result = participationValidator.tryParticipate(123L, 1000);
        assertEquals(EventJoinResult.SOLD_OUT, result);
    }
}
