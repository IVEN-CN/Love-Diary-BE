package com.iven.memo.models.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoUpdateDTO {
    @NotBlank(message = "用户名不能为空")
    private String userName;        // 用户名
    @NotBlank(message = "昵称不能为空")
    private String nickName;        // 昵称
}
