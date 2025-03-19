package com.shop.shoppingevent.controller;

import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private static final String EVENT_TICKET_COUNT_KEY = "event:ticket:count";
    private static final String EVENT_PARTICIPATION_KEY = "event:participants";

    private static final int EVENT_TICKET_LIMIT = 950;

    private final StringRedisTemplate redisTemplate;
    private final EventService eventService;
    private final KafkaTemplate<String, EventPointMessage> kafkaTemplate;

    // 현재 쿠폰 개수와 상태 체크
    @GetMapping("/ticket/status")
    public ResponseEntity<EventStatusResponse> getTicketStatus() {
        int remainingTickets = getRemainingTickets();
        boolean isAvailable = remainingTickets > 0;
        EventStatusResponse response = new EventStatusResponse(isAvailable, remainingTickets);
        return ResponseEntity.ok(response);
    }

    // 이벤트 참여 처리
    @PostMapping("/ticket/apply")
    public ResponseEntity<EventApplyResponse> applyEvent(@RequestBody TicketApplyRequest ticketApplyRequest) {
        log.info("Request to apply event: {}", ticketApplyRequest);
        Long userId = ticketApplyRequest.getUserId();
        Long ticketCount = redisTemplate.opsForValue().increment(EVENT_TICKET_COUNT_KEY);
        Boolean isParticipated = redisTemplate.opsForSet().isMember(EVENT_PARTICIPATION_KEY, userId.toString());

        /// TODO validateApplyEvent 에서 예외 발생 후 catch로 처리하도록 수정
        ResponseEntity<EventApplyResponse> validateResponse = validateApplyEvent(isParticipated, ticketCount);
        if (validateResponse != null)
            return validateResponse;

        eventService.saveParticipation(ticketApplyRequest.getUserId());
        redisTemplate.opsForSet().add(EVENT_PARTICIPATION_KEY, userId.toString());
        kafkaTemplate.send("event-point-topic", new EventPointMessage(ticketApplyRequest.getUserId(), ticketApplyRequest.getReason(), 10000, 1));
        return ResponseEntity.ok(new EventApplyResponse(true, "이벤트 참여에 성공했습니다."));
    }

    private static ResponseEntity<EventApplyResponse> validateApplyEvent(Boolean isParticipated, Long ticketCount) {
        if (isParticipated != null && isParticipated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "이미 이벤트에 참여하셨습니다."));
        }

        if (ticketCount == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EventApplyResponse(false, "이벤트 쿠폰 발급에 실패했습니다. 다시 시도해주세요."));
        } else if (ticketCount > EVENT_TICKET_LIMIT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "모든 쿠폰이 소진되었습니다."));
        }
        return null;
    }

    private int getRemainingTickets() {
        String count = redisTemplate.opsForValue().get(EVENT_TICKET_COUNT_KEY);
        int issuedTickets = count == null ? 0 : Integer.parseInt(count);
        int remaining = EVENT_TICKET_LIMIT - issuedTickets;
        return Math.max(remaining, 0);
    }

    record EventStatusResponse(boolean available, int remainingTickets) {
    }

    record EventApplyResponse(boolean success, String message) {
    }
}
