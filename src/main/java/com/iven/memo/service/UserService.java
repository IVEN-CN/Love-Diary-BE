package com.iven.memo.service;

import com.iven.memo.models.DTO.User.*;

public interface UserService {
    UserTokenResponseDTO login(String username, String password);

    /**
     * token续期
     * @return 包含续期后的token的用户信息
     */
    UserTokenResponseDTO extension();

    /**
     * 更新用户信息
     * @param updateDTO 用户信息更新DTO
     * @return 更新后的用户信息展示DTO
     */
    UserInfoDisplayDTO update(UserInfoUpdateDTO updateDTO);

    /**
     * 更新用户密码
     * @param pwdUpdateDTO 用户密码更新DTO
     */
    void updatePwd(UserPwdUpdateDTO pwdUpdateDTO);

    /**
     * 获取情人用户信息
     * @return 伴侣的用户信息
     */
    UserInfoDisplayDTO getLoverInfo();

    /**
     * 绑定伴侣
     *
     * @param userName 包含伴侣的用户名
     * @return
     */
    void bindLover(OnlyUserNameDTO userName);

    /**
     * 解绑伴侣
     */
    void deBindLover();

    /**
     * 接受绑定伴侣请求
     * @param link 短链链接
     */
    void acceptBindLover(String link);
}
