package com.shop.shoppingevent.service;

import com.shop.shoppingevent.dto.EventJoinResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.shop.shoppingevent.dto.EventJoinResult.*;

@Service
@RequiredArgsConstructor
public class EventParticipationValidator {

    private static final String luaScriptWithoutTTL = """
            local alreadyJoined = redis.call("SISMEMBER", KEYS[1], ARGV[1])
            if alreadyJoined == 1 then
                return 0
            end
            
            local currentCount = tonumber(redis.call("GET", KEYS[2]) or "0")
            if currentCount >= tonumber(ARGV[2]) then
                return -1
            end
            
            redis.call("SADD", KEYS[1], ARGV[1])
            redis.call("INCR", KEYS[2])
            return 1
            """;
    private final RedisTemplate<String, Object> redisTemplate;

    public EventJoinResult tryParticipate(Long userId, int maxCount) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScriptWithoutTTL);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(
                script,
                Arrays.asList("event:participants", "event:count"),
                userId.toString(),
                maxCount
        );

        return switch (result.intValue()) {
            case 1 -> SUCCESS;
            case 0 -> ALREADY_JOINED;
            case -1 -> SOLD_OUT;
            default -> SOLD_OUT;
        };
    }
}
