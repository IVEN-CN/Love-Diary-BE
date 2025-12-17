package com.iven.memo.models.DTO.BindInvite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绑定邀请记录DTO（返回给前端）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindInviteRecordDTO {
    private Long fromUserId;            // 发起邀请的用户ID
    private String fromUserName;        // 发起邀请的用户名
    private String link;                // 邀请短链
    private LocalDateTime createTime;   // 创建时间
    private Boolean hasResponse;        // 是否已响应（true=已响应，false=未响应，null=不适用）
    private Boolean accepted;           // 是否接受（true=接受，false=拒绝，null=未响应或不适用）
}
