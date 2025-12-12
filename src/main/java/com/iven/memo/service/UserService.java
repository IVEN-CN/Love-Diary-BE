package com.iven.memo.service;

import com.iven.memo.models.DTO.User.UserTokenResponseDTO;

public interface UserService {
    UserTokenResponseDTO login(String username, String password);

    /**
     * token续期
     * @return 包含续期后的token的用户信息
     */
    UserTokenResponseDTO extension();
}
