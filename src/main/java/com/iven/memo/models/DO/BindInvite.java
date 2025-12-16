package com.iven.memo.models.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绑定邀请表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindInvite {
    private Long id;                        // 主键ID
    private Long fromUserId;                // 发起邀请的用户ID
    private Long toUserId;                  // 被邀请用户ID
    private LocalDateTime expireTime;       // 邀请码过期时间
    @Builder.Default
    private boolean used = false;           // 邀请是否已被使用
}
