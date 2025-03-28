package com.shop.shoppingevent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 테스트용
public class TicketApplyRequest {

    @NotNull
    private Long userId;
    private Integer ticketNumber;
    private String reason;
}
