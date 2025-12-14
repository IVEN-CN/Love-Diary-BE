package com.iven.memo.models.DTO.User;

import com.iven.memo.models.DO.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UserInfoDisplayDTO {
    private Long id;
    private String userName;        // 用户名
    private String nickName;        // 昵称
    private LocalDate birthday;        // 生日
    private String avatar;          // 头像

    public UserInfoDisplayDTO(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.nickName = user.getNickName();
        this.birthday = user.getBirthday();
        this.avatar = user.getAvatar();
    }
}
