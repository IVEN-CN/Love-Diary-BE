package com.iven.memo.models.Enumerate;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 系统消息类型枚举
 */
public enum SystemMessageType {
    INVITE("INVITE"),       // 邀请消息（被邀请用户看到）
    RESPONSE("RESPONSE");   // 响应消息（邀请发起人看到）

    private final String value;

    SystemMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
