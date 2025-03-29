package com.shop.shoppingevent.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class PointEvent<V> {
    String topic;
    V data;
}
