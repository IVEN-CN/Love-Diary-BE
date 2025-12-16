package com.iven.memo.models.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoverBindMessage {
    private Long fromUserId;        // 发起绑定邀请的用户id
    private String fromUserName;    // 发起绑定邀请的用户名
    private String link;            // 接收邀请的短链链接
}
