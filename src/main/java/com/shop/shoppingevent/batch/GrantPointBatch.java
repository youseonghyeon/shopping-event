package com.shop.shoppingevent.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.shoppingevent.dto.EventPointMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true") // 단일 서버에서만 처리
public class GrantPointBatch {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "event-point-topic", groupId = "event-point-group")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            EventPointMessage message = objectMapper.readValue(record.value(), EventPointMessage.class);
            log.info("message: {}", message);
            // TODO 이벤트 지급 처리
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
