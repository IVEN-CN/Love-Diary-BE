package com.iven.memo.models.DTO.BindInvite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绑定响应记录DTO（返回给前端）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindResponseRecordDTO {
    private Long responseUserId;        // 响应用户ID
    private String responseUserName;    // 响应用户名
    private String link;                // 邀请短链
    private boolean accepted;           // 是否接受邀请（true=接受，false=拒绝）
    private LocalDateTime responseTime; // 响应时间
}
