package com.iven.memo.models.DTO.User;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoDTO {
    private Long id;
    private String userName;        // 用户名
    private String nickName;        // 昵称
    private LocalDate birthday;        // 生日
    private String avatar;          // 头像
}
