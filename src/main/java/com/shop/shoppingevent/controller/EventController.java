package com.shop.shoppingevent.controller;

import com.shop.shoppingevent.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private static final String EVENT_COUPON_COUNT_KEY = "event:coupon:count";
    private static final int EVENT_COUPON_LIMIT = 950;

    private final StringRedisTemplate redisTemplate;
    private final EventService eventService;

    // 현재 쿠폰 개수와 상태 체크
    @GetMapping("/coupon/status")
    public ResponseEntity<EventStatusResponse> getCouponStatus() {
        int remainingCoupons = getRemainingCoupons();
        boolean isAvailable = remainingCoupons > 0;

        EventStatusResponse response = new EventStatusResponse(isAvailable, remainingCoupons);
        return ResponseEntity.ok(response);
    }

    // 이벤트 참여 처리
    @PostMapping("/coupon/apply")
    public ResponseEntity<EventApplyResponse> applyEvent(@RequestParam("userId") Long userId) {
        Long couponCount = redisTemplate.opsForValue().increment(EVENT_COUPON_COUNT_KEY);

        if (couponCount > EVENT_COUPON_LIMIT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "모든 쿠폰이 소진되었습니다."));
        }

        // TODO 이벤트 DB에 사용자 이벤트 참여 기록 저장 (이후 Kafka 발행 후 포인트 지급)
        eventService.saveParticipation(userId);

        return ResponseEntity.ok(new EventApplyResponse(true, "이벤트 참여에 성공했습니다."));
    }

    private int getRemainingCoupons() {
        String count = redisTemplate.opsForValue().get(EVENT_COUPON_COUNT_KEY);
        int issuedCoupons = count == null ? 0 : Integer.parseInt(count);
        int remaining = EVENT_COUPON_LIMIT - issuedCoupons;
        return Math.max(remaining, 0);
    }

    // 응답 DTO
    record EventStatusResponse(boolean available, int remainingCoupons) {
    }

    record EventApplyResponse(boolean success, String message) {
    }
}
