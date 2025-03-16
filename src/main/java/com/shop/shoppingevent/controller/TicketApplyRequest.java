package com.shop.shoppingevent.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 테스트용
public class TicketApplyRequest {

    private Long userId;
    private Integer ticketNumber;
    private String reason;
}
