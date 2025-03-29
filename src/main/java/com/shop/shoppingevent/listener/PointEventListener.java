package com.shop.shoppingevent.listener;

import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.listener.dto.PointEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final KafkaTemplate<String, EventPointMessage> kafkaTemplate;

    @Async
    @EventListener(PointEvent.class)
    public void handleOrderSubmittedEvent(PointEvent<EventPointMessage> event) {
        log.info("Received event point message: {}", event.getData());
        try {
            kafkaTemplate.send(event.getTopic(), event.getData());
        } catch (Exception e) {
            log.error("Error sending event point message to Kafka: {}, message: {}", e.getMessage(), event.getData(), e);
            // TODO DB 또는 Redis에 실패한 메시지 저장 후 메뉴얼로 사용자에게 포인트 부여 필요
        }
    }
}
