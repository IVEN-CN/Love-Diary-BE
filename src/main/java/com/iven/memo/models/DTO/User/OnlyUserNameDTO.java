package com.iven.memo.models.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnlyUserNameDTO {
    @NotBlank(message = "用户名不能为空")
    String username;
}
