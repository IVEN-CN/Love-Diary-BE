package com.iven.memo.models.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserInfoUpdateDTO {
    @NotBlank(message = "用户名不能为空")
    private String userName;        // 用户名
    @NotBlank(message = "昵称不能为空")
    private String nickName;        // 昵称
}
