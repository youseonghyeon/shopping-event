package com.shop.shoppingevent.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPointMessage {
    private Long userId;
    private int point;
    private String reason; // 지급 이유 (이벤트 명 등)
}
