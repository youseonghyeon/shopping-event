package com.shop.shoppingevent.controller;

import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.service.EventJoinResult;
import com.shop.shoppingevent.service.EventParticipationValidator;
import com.shop.shoppingevent.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private static final int EVENT_TICKET_LIMIT = 1000;

    private final StringRedisTemplate redisTemplate;
    private final EventService eventService;
    private final KafkaTemplate<String, EventPointMessage> kafkaTemplate;
    private final EventParticipationValidator participationValidator;

    // 이벤트 참여 처리
    @PostMapping("/ticket/apply")
    public ResponseEntity<EventApplyResponse> applyEvent(@RequestBody TicketApplyRequest ticketApplyRequest) {
        log.info("Request to apply event: {}", ticketApplyRequest);

        Long userId = ticketApplyRequest.getUserId();
        EventJoinResult eventJoinResult = participationValidator.tryParticipate(userId, EVENT_TICKET_LIMIT);

        if (EventJoinResult.ALREADY_JOINED.equals(eventJoinResult)) {
            log.info("User already joined the event: {}", ticketApplyRequest);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EventApplyResponse(false, "이미 이벤트에 참여하셨습니다."));
        } else if (EventJoinResult.SOLD_OUT.equals(eventJoinResult)) {
            log.info("Event is sold out: {}", ticketApplyRequest);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EventApplyResponse(false, "모든 쿠폰이 소진되었습니다."));
        }

        log.info("User joined the event: {}", ticketApplyRequest);
        eventService.saveParticipation(ticketApplyRequest.getUserId());
        kafkaTemplate.send("event-point-topic", new EventPointMessage(ticketApplyRequest.getUserId(), ticketApplyRequest.getReason(), 10000, 1));
        return ResponseEntity.ok(new EventApplyResponse(true, "이벤트 참여에 성공했습니다."));
    }

    record EventStatusResponse(boolean available, int remainingTickets) {
    }

    record EventApplyResponse(boolean success, String message) {
    }
}
