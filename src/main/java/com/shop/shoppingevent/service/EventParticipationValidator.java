package com.shop.shoppingevent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EventParticipationValidator {

    private final RedisTemplate<String, Object> redisTemplate;

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
            case 1 -> EventJoinResult.SUCCESS;
            case 0 -> EventJoinResult.ALREADY_JOINED;
            case -1 -> EventJoinResult.SOLD_OUT;
            default -> EventJoinResult.SOLD_OUT;
        };
    }
}
