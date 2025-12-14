package com.iven.memo.models.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserPwdUpdateDTO {
    @NotBlank(message = "原始密码不能为空")
    private String oriPwd;
    @NotBlank(message = "新密码不能为空")
    private String newPwd;
}
