package com.iven.memo.models.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPwdUpdateDTO {
    @NotBlank(message = "原始密码不能为空")
    private String oriPwd;
    @NotBlank(message = "新密码不能为空")
    private String newPwd;
}
