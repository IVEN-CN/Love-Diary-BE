package com.iven.memo.models.DTO.BindInvite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统消息DTO（包含邀请消息和响应消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMessageDTO {
    private List<BindInviteRecordDTO> inviteMessages;    // 邀请消息列表（被邀请人看到）
    private List<BindResponseRecordDTO> responseMessages; // 响应消息列表（邀请发起人看到）
}
