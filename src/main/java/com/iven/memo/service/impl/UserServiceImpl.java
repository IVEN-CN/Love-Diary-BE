package com.iven.memo.service.impl;

import com.iven.memo.exceptions.GlobalException;
import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.UserInfoDisplayDTO;
import com.iven.memo.models.DTO.User.UserInfoUpdateDTO;
import com.iven.memo.models.DTO.User.UserPwdUpdateDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.service.UserService;
import com.iven.memo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

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

    @Override
    public UserInfoDisplayDTO update(UserInfoUpdateDTO updateDTO) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (user != null) {
            BeanUtils.copyProperties(updateDTO, user);
            log.info("更新用户信息: {}", user);
            int influence = userMapper.updateById(user);
            log.info("mybatis update User {}", user);

            if (influence <= 0) {
                throw new GlobalException("更新用户信息时，数据库操作失败，mybatis影响行数为0");
            }

            UserInfoDisplayDTO userInfoDisplayDTO = new UserInfoDisplayDTO();
            BeanUtils.copyProperties(user, userInfoDisplayDTO);

            return userInfoDisplayDTO;
        } else {
            throw new LoginFail("用户未登录，无法更新");
        }
    }

    @Override
    public void updatePwd(UserPwdUpdateDTO pwdUpdateDTO) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (user != null) {
            // 校验旧密码
            if (!user.getPassword().equals(pwdUpdateDTO.getOriPwd())) {
                throw new LoginFail("旧密码错误，无法更新密码");
            }

            // 更新为新密码
            user.setPassword(pwdUpdateDTO.getNewPwd());
            log.info("更新用户密码: {}", user);
            int influence = userMapper.updateById(user);
            log.info("mybatis update User {}", user);

            if (influence <= 0) {
                throw new GlobalException("更新用户密码时，数据库操作失败，mybatis影响行数为0");
            }
        } else {
            throw new LoginFail("用户未登录，无法更新");
        }
    }
}
