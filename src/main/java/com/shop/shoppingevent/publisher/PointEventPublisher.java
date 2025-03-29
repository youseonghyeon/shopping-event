package com.shop.shoppingevent.publisher;

import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.dto.TicketApplyRequest;
import com.shop.shoppingevent.listener.dto.PointEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEventPoint(TicketApplyRequest ticketApplyRequest, int point) {
        // kafka producer 로 포인트 지급 이벤트 전송
        EventPointMessage data = new EventPointMessage(ticketApplyRequest.getUserId(), ticketApplyRequest.getReason(), point, 1);
        PointEvent<EventPointMessage> event = new PointEvent<>("event-point-topic", data);
        log.info("publish event point data: {}", data);
        applicationEventPublisher.publishEvent(event);
    }
}
