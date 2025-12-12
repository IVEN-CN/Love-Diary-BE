package com.iven.memo.controller;

import com.iven.memo.BaseTest;
import com.iven.memo.LoginTest;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.UserLoginRequestDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class UserControllerTest extends BaseTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void login() throws Exception {
        // 数据准备
        User user = User.builder()
                .birthday(LocalDate.now())
                .userName("test_user")
                .password("123456")
                .build();
        userMapper.insert(user);
        log.info("mybatis insert user in UserControllerTest: {}", user);

        // 测试登录接口
        UserLoginRequestDTO requestDTO = UserLoginRequestDTO.builder()
                .userName("test_user")
                .password("123456")
                .build();
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO))
        ).andDo(result -> {
            String responseContent = result.getResponse().getContentAsString();
            log.info("Login response: {}", responseContent);
            assertTrue(responseContent.contains("token"));
        }).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void extensionToken() throws Exception {
        // 数据准备
        User user = User.builder()
                .birthday(LocalDate.now())
                .userName("test_user")
                .password("123456")
                .build();
        userMapper.insert(user);
        log.info("mybatis insert user in extensionToken: {}", user);

        String token = jwtUtil.generateToken(user.getId());
        AtomicReference<String> newToken = new AtomicReference<>();

        mockMvc.perform(MockMvcRequestBuilders.post("/users/extension")
                        .header("Authorization", "Bearer " + token)
        ).andDo(result -> {
            String responseContent = result.getResponse().getContentAsString();
            log.info("Extension response: {}", responseContent);
            assertTrue(responseContent.contains("token"));
            ResponseMessage<?> responseDTO = objectMapper.readValue(responseContent, ResponseMessage.class);
            UserTokenResponseDTO userTokenResponseDTO = objectMapper.readValue(
                    objectMapper.writeValueAsString(responseDTO.getDetails()),
                    UserTokenResponseDTO.class
            );
            newToken.set(userTokenResponseDTO.getToken());
        }).andExpect(MockMvcResultMatchers.status().isOk());

        // 用newToken验证能否解析出正确的用户ID
        String userId = jwtUtil.validateToken(newToken.get());
        Assertions.assertEquals(String.valueOf(user.getId()), userId);
    }
}