package com.shop.shoppingevent.controller;

import com.shop.shoppingevent.dto.EventJoinResult;
import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.dto.TicketApplyRequest;
import com.shop.shoppingevent.service.EventParticipationValidator;
import com.shop.shoppingevent.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private static final int EVENT_TICKET_LIMIT = 1000;

    private final EventService eventService;
    private final KafkaTemplate<String, EventPointMessage> kafkaTemplate;
    private final EventParticipationValidator participationValidator;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = String.format("[shopping-event] is UP (%s)", now);
        return ResponseEntity.ok(message);
    }

    // 이벤트 상태 조회
    @GetMapping("/ticket/status")
    public ResponseEntity<EventStatusResponse> getEventStatus() {
        int remainingTickets = EVENT_TICKET_LIMIT - eventService.countParticipation();
        boolean available = remainingTickets > 0;
        return ResponseEntity.ok(new EventStatusResponse(available, remainingTickets));
    }

    // 이벤트 참여 처리
    @PostMapping("/ticket/apply")
    public ResponseEntity<EventApplyResponse> applyEvent(@RequestBody TicketApplyRequest ticketApplyRequest) {
        log.info("Request to apply event: {}", ticketApplyRequest);

        Long userId = ticketApplyRequest.getUserId();
        EventJoinResult eventJoinResult = participationValidator.tryParticipate(userId, EVENT_TICKET_LIMIT);

        if (!eventJoinResult.isSuccess()) {
            return buildEventApplyErrorResponse(eventJoinResult);
        }

        log.info("User joined the event: {}", ticketApplyRequest);
        eventService.saveParticipation(ticketApplyRequest.getUserId());
        kafkaTemplate.send("event-point-topic", new EventPointMessage(ticketApplyRequest.getUserId(), ticketApplyRequest.getReason(), 10000, 1));
        return ResponseEntity.ok(new EventApplyResponse(true, "이벤트 참여에 성공했습니다."));
    }

    private static ResponseEntity<EventApplyResponse> buildEventApplyErrorResponse(EventJoinResult eventJoinResult) {
        return switch (eventJoinResult) {
            case ALREADY_JOINED -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "이미 이벤트에 참여하셨습니다."));
            case SOLD_OUT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "모든 쿠폰이 소진되었습니다."));
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EventApplyResponse(false, "이벤트 참여에 실패했습니다."));
        };
    }

    record EventStatusResponse(boolean available, int remainingTickets) {
    }

    record EventApplyResponse(boolean success, String message) {
    }
}
