package com.iven.memo.controller;

import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.*;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseMessage<UserTokenResponseDTO> login(@RequestBody @Valid UserLoginRequestDTO requestDTO) {
        UserTokenResponseDTO responseDTO = userService.login(requestDTO.getUserName(), requestDTO.getPassword());
        return ResponseMessage.success(responseDTO);
    }

    @PostMapping("/extension")
    public ResponseMessage<UserTokenResponseDTO> extensionToken() {
        UserTokenResponseDTO responseDTO = userService.extension();
        return ResponseMessage.success(responseDTO);
    }

    @GetMapping("/jwt2user")
    public ResponseMessage<UserInfoDisplayDTO> getUserInfoDTO() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        UserInfoDisplayDTO responseDTO = new UserInfoDisplayDTO();
        if (currentUser != null) {
            BeanUtils.copyProperties(currentUser, responseDTO);
            return ResponseMessage.success(responseDTO);
        } else {
            return ResponseMessage.error(new LoginFail("无法获取用户信息"));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping()
    public ResponseMessage<UserInfoDisplayDTO> updateUserInfo(@RequestBody @Valid UserInfoUpdateDTO updateDTO) {
        UserInfoDisplayDTO responseDTO = userService.update(updateDTO);
        return ResponseMessage.success(responseDTO);
    }

    /**
     * 更新密码
     */
    @PutMapping("/pwd")
    public ResponseMessage<Void> updatePassword(@RequestBody @Valid UserPwdUpdateDTO pwdUpdateDTO) {
        userService.updatePwd(pwdUpdateDTO);
        return ResponseMessage.success();
    }
}
