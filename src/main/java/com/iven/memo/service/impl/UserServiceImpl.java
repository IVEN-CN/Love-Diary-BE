package com.iven.memo.service.impl;

import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.service.UserService;
import com.iven.memo.utils.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserTokenResponseDTO login(String username, String password) {
        Optional<User> userOptional = userMapper.findByNameAndPassword(username, password);
        if (userOptional.isEmpty()) {
            throw new LoginFail("登录失败，用户名或密码错误");
        }
        return UserTokenResponseDTO.builder()
                .userName(userOptional.get().getUserName())
                .nickName(userOptional.get().getNickName())
                .birthday(userOptional.get().getBirthday())
                .token(jwtUtil.generateToken(userOptional.get().getId()))
                .build();
    }

    @Override
    public UserTokenResponseDTO extension() {
        //从上下文拿到User
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        // 生成新的token并返回用户信息
        if (user != null) {
            return UserTokenResponseDTO.builder()
                    .userName(user.getUserName())
                    .nickName(user.getNickName())
                    .birthday(user.getBirthday())
                    .token(jwtUtil.generateToken(user.getId()))
                    .build();
        } else {
            throw new LoginFail("用户未登录，无法续期");
        }
    }
}
