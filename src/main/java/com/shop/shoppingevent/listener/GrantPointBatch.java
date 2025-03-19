package com.shop.shoppingevent.listener;

import com.shop.shoppingevent.dto.EventPointMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(name = "feature.point-consumer.enabled", havingValue = "true")
public class GrantPointBatch {

    private final JdbcTemplate businessJdbcTemplate;
    private final JdbcTemplate eventJdbcTemplate;

    public GrantPointBatch(@Qualifier("businessJdbcTemplate") JdbcTemplate businessJdbcTemplate,
                           @Qualifier("eventJdbcTemplate") JdbcTemplate eventJdbcTemplate) {
        this.businessJdbcTemplate = businessJdbcTemplate;
        this.eventJdbcTemplate = eventJdbcTemplate;
    }

    @KafkaListener(topics = "event-point-topic", groupId = "event-point-group")
    @Transactional
    public void listen(ConsumerRecord<String, EventPointMessage> record) {
        try {
            EventPointMessage eventPointMessage = record.value();
            log.info("Received event point message: {}", eventPointMessage);

            // 이벤트 포인트 지급 처리
            String sql = "update users set point = point + ? where id = ?";
            businessJdbcTemplate.update(sql, eventPointMessage.getPoint(), eventPointMessage.getUserId());

            // 이벤트 포인트 지급 완료 후 완료 insert 처리
            String eventSql = "update participation set given = true where user_id = ?";
            eventJdbcTemplate.update(eventSql, eventPointMessage.getUserId());
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
