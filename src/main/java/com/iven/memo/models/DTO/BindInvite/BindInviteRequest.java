package com.iven.memo.models.DTO.BindInvite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求绑定
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindInviteRequest {
    private String link;
    private String fromUserName;
}
