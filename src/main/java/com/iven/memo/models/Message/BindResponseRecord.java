package com.iven.memo.models.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 绑定响应记录（存储在Redis中）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindResponseRecord implements Serializable {
    private Long fromUserId;            // 原始邀请发起人的用户ID
    private String link;                // 邀请短链（用于标识哪个邀请被响应）
    private Long responseUserId;        // 响应用户ID（接受或拒绝邀请的用户）
    private String responseUserName;    // 响应用户名
    private boolean accepted;           // 是否接受邀请（true=接受，false=拒绝）
    private LocalDateTime responseTime; // 响应时间
}
