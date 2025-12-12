package com.iven.memo.models.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String userName;        // 用户名
    private String nickName;        // 昵称
    private String password;        // 密码
    private LocalDate birthday;     // 生日
    private String avatar;          // 头像
}
