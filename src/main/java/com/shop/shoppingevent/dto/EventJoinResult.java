package com.shop.shoppingevent.dto;

import lombok.Getter;

@Getter
public enum EventJoinResult {
    SUCCESS("이벤트 참여에 성공하였습니다."),
    ALREADY_JOINED("이미 이벤트에 참여하셨습니다."),
    SOLD_OUT("모든 쿠폰이 소진되었습니다.");

    private final String message;

    private EventJoinResult(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(this);
    }

}
