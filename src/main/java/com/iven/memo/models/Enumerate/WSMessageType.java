package com.iven.memo.models.Enumerate;

public enum WSMessageType {
    LOVER_BIND,         // 伴侣绑定消息
    LOVER_DEBIND,       // 伴侣解绑消息
    BIND_ACCEPT,        // 接受绑定邀请
    BIND_REJECT,        // 拒绝绑定邀请
    CHAT,               // 聊天消息
    SYSTEM              // 系统消息
}
