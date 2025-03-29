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
public class GrantPointListener {

    private final JdbcTemplate businessJdbcTemplate;
    private final JdbcTemplate eventJdbcTemplate;

    public GrantPointListener(@Qualifier("businessJdbcTemplate") JdbcTemplate businessJdbcTemplate,
                              @Qualifier("eventJdbcTemplate") JdbcTemplate eventJdbcTemplate) {
        this.businessJdbcTemplate = businessJdbcTemplate;
        this.eventJdbcTemplate = eventJdbcTemplate;
    }

    @Transactional
    @KafkaListener(topics = "event-point-topic", groupId = "event-point-group")
    public void listen(ConsumerRecord<String, EventPointMessage> record) {
        try {
            EventPointMessage eventPointMessage = record.value();
            log.info("Received event point message: {}", eventPointMessage);

            // 이벤트 포인트 지급 처리
            grantUserPoint(eventPointMessage);
            // 이벤트 포인트 지급 완료 처리
            markParticipationAsGiven(eventPointMessage);
        } catch (Exception e) {
            log.error("Error saving event point to Databases: {}, message: {}", e.getMessage(), record.value(), e);
            // TODO 만약 DB에 저장이 되지 않는다고 한다면 File, Redis 또는 로그로 저장 후 메뉴얼로 사용자에게 포인트 부여 필요
            // TODO 또는 재처리 필요
        }
    }

    private void grantUserPoint(EventPointMessage eventPointMessage) {
        try {
            String sql = "update users set point = point + ? where id = ?";
            businessJdbcTemplate.update(sql, eventPointMessage.getPoint(), eventPointMessage.getUserId());
        } catch (Exception e) {
            log.error("Error while granting point to user: {}, message: {}", e.getMessage(), eventPointMessage, e);
            // ROLLBACK 후 복구 전략에 따라 후처리
            throw new RuntimeException(e);
        }
    }

    private void markParticipationAsGiven(EventPointMessage eventPointMessage) {
        try {
            String eventSql = "update participation set given = true where user_id = ?";
            eventJdbcTemplate.update(eventSql, eventPointMessage.getUserId());
        } catch (Exception e) {
            log.error("Error while marking participation as given: {}, message: {}", e.getMessage(), eventPointMessage, e);
            // ROLLBACK 후 복구 전략에 따라 후처리
            throw new RuntimeException(e);
        }
    }
}
