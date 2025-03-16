package com.shop.shoppingevent.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EventPointMessage {
    private Long userId;
    private String reason;
    private Integer point;
    private Integer ticketNumber;
}
