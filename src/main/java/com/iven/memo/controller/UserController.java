package com.iven.memo.controller;

import com.iven.memo.models.DTO.User.UserLoginRequestDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
