package com.shop.shoppingevent.controller;

import com.shop.shoppingevent.dto.EventPointMessage;
import com.shop.shoppingevent.service.EventParticipationValidator;
import com.shop.shoppingevent.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.shop.shoppingevent.dto.EventJoinResult.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @Mock
    private KafkaTemplate<String, EventPointMessage> kafkaTemplate;

    @Mock
    private EventParticipationValidator participationValidator;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setup() {
        // standaloneSetup을 사용하여 Spring 컨텍스트 없이 테스트를 구성
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    @DisplayName("헬스 체크 엔드포인트 테스트")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/event/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("[shopping-event] is UP")));
    }

    @Test
    @DisplayName("이벤트 상태 조회 테스트 - 남은 티켓이 있는 경우")
    void testGetEventStatus() throws Exception {
        // 예: 참여 수가 900이면 남은 티켓은 100이어야 함
        when(eventService.countParticipation()).thenReturn(900);
        mockMvc.perform(get("/event/ticket/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.remainingTickets").value(100));
    }

    @Test
    @DisplayName("이벤트 참여 테스트 - 성공 케이스")
    void testApplyEventSuccess() throws Exception {
        // SUCCESS 반환 시
        when(participationValidator.tryParticipate(anyLong(), eq(1000))).thenReturn(SUCCESS);
        doNothing().when(eventService).saveParticipation(anyLong());
        String requestBody = "{\"userId\":12345, \"reason\":\"테스트 참여\"}";

        mockMvc.perform(post("/event/ticket/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 참여에 성공했습니다."));
    }

    @Test
    @DisplayName("이벤트 참여 테스트 - 이미 참여한 경우")
    void testApplyEventAlreadyJoined() throws Exception {
        when(participationValidator.tryParticipate(anyLong(), eq(1000))).thenReturn(ALREADY_JOINED);
        String requestBody = "{\"userId\":12345, \"reason\":\"테스트 참여\"}";

        mockMvc.perform(post("/event/ticket/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 이벤트에 참여하셨습니다."));
    }

    @Test
    @DisplayName("이벤트 참여 테스트 - 매진된 경우")
    void testApplyEventSoldOut() throws Exception {
        when(participationValidator.tryParticipate(anyLong(), eq(1000))).thenReturn(SOLD_OUT);
        String requestBody = "{\"userId\":12345, \"reason\":\"테스트 참여\"}";

        mockMvc.perform(post("/event/ticket/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("모든 쿠폰이 소진되었습니다."));
    }
}
