package com.iven.memo.controller;

import com.iven.memo.exceptions.JwtForbidden;
import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.UserInfoDTO;
import com.iven.memo.models.DTO.User.UserLoginRequestDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
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
    public ResponseMessage<UserInfoDTO> getUserInfoDTO() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        UserInfoDTO responseDTO = new UserInfoDTO();
        if (currentUser != null) {
            BeanUtils.copyProperties(currentUser, responseDTO);
            return ResponseMessage.success(responseDTO);
        } else {
            return ResponseMessage.error(new LoginFail("无法获取用户信息"));
        }
    }
}
