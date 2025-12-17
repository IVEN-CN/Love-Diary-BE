package com.iven.memo.models.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 绑定邀请记录（存储在Redis中）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindInviteRecord implements Serializable {
    private Long fromUserId;            // 发起邀请的用户ID
    private String fromUserName;        // 发起邀请的用户名
    private Long toUserId;              // 被邀请用户ID
    private String link;                // 邀请短链
    private LocalDateTime createTime;   // 创建时间
}
