package com.iven.memo.service;

import com.iven.memo.models.DTO.User.UserInfoDisplayDTO;
import com.iven.memo.models.DTO.User.UserInfoUpdateDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;

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
}
