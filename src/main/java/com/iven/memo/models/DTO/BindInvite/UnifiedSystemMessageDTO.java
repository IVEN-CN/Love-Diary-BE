package com.iven.memo.models.DTO.BindInvite;

import com.iven.memo.models.Enumerate.SystemMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一系统消息DTO（包含邀请消息和响应消息，统一展示）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnifiedSystemMessageDTO {
    private SystemMessageType messageType;  // 消息类型：INVITE（邀请消息）或 RESPONSE（响应消息）
    
    // 邀请消息字段
    private Long fromUserId;            // 发起邀请的用户ID
    private String fromUserName;        // 发起邀请的用户名
    
    // 响应消息字段
    private Long responseUserId;        // 响应用户ID
    private String responseUserName;    // 响应用户名
    
    // 公共字段
    private String link;                // 邀请短链
    private LocalDateTime time;         // 消息时间（邀请创建时间或响应时间）
    
    // 状态字段
    private Boolean hasResponse;        // 是否已响应（仅邀请消息有效）
    private Boolean accepted;           // 是否接受（邀请消息：响应结果，响应消息：响应状态）
}
