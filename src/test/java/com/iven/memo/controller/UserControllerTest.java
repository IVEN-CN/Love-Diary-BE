package com.iven.memo.controller;

import com.iven.memo.BaseTest;
import com.iven.memo.LoginTest;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.UserInfoUpdateDTO;
import com.iven.memo.models.DTO.User.UserLoginRequestDTO;
import com.iven.memo.models.DTO.User.UserPwdUpdateDTO;
import com.iven.memo.models.DTO.User.UserTokenResponseDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;
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

    @Test
    void getUserInfoDTO() throws Exception {
        // 创建并保存测试用户
        User user = User.builder()
                .birthday(LocalDate.of(2000, 1, 1))
                .userName("test_user")
                .password("123456")
                .nickName("TestNick") // 确保DTO有对应字段
                .build();
        userMapper.insert(user);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId());

        // 调用接口
        mockMvc.perform(MockMvcRequestBuilders.get("/users/jwt2user")
                        .header("Authorization", "Bearer " + token))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UserInfoDTO response: {}", responseContent);
                    assertTrue(responseContent.contains("userName"));
                    assertTrue(responseContent.contains("test_user"));
                    assertTrue(responseContent.contains("TestNick")); // 判断昵称字段
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updateUserInfo() throws Exception {
        // 创建并保存原始用户
        User user = User.builder()
                .birthday(LocalDate.of(2000, 1, 1))
                .userName("test_user")
                .password("123456")
                .nickName("OldNick") // 旧昵称
                .build();
        userMapper.insert(user);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId());

        // 构造新的用户信息
        UserInfoUpdateDTO updateDTO = UserInfoUpdateDTO.builder()
                .nickName("NewNick") // 更新昵称
                .userName("NewUserName") // 更新用户名
                .build();

        // 调用接口
        mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UpdateUserInfo response: {}", responseContent);
                    assertTrue(responseContent.contains("NewNick")); // 确认昵称已更新
                    assertTrue(responseContent.contains("NewUserName")); // 确认用户名已更新
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void updatePassword() throws Exception {
        // 创建并保存原始用户
        User user = User.builder()
                .birthday(LocalDate.now())
                .userName("test_user")
                .password("123456") // 原密码
                .build();
        userMapper.insert(user);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId());

        // 构造密码更新请求数据
        UserPwdUpdateDTO pwdUpdateDTO = UserPwdUpdateDTO.builder()
                .oriPwd("123456") // 原密码
                .newPwd("654321") // 新密码
                .build();

        // 调用接口
        mockMvc.perform(MockMvcRequestBuilders.put("/users/pwd")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pwdUpdateDTO)))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UpdatePassword response: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 校验数据库密码是否已更新
        Optional<User> updated = userMapper.findById(user.getId());
        Assertions.assertTrue(updated.isPresent());
        Assertions.assertEquals("654321", updated.get().getPassword());
    }

    @Test
    void getLoverInfoSuccess() throws Exception {
        // 准备数据：创建两个用户并建立伴侣关系
        User user1 = User.builder()
                .userName("user1")
                .password("123456")
                .nickName("用户一")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .userName("user2")
                .password("654321")
                .nickName("用户二")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();
        userMapper.insert(user1);
        userMapper.insert(user2);

        // 绑定伴侣关系（user1的伴侣是user2）
        user1.setLoverId(user2.getId());
        userMapper.updateById(user1);

        // 生成user1的令牌
        String token = jwtUtil.generateToken(user1.getId());

        // 调用获取伴侣信息接口
        mockMvc.perform(MockMvcRequestBuilders.get("/users/lover")
                        .header("Authorization", "Bearer " + token))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("GetLoverInfo response: {}", responseContent);
                    // 验证响应包含伴侣信息
                    assertTrue(responseContent.contains("user2"));
                    assertTrue(responseContent.contains("用户二"));
                    assertTrue(responseContent.contains("1992-02-02"));
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getLoverInfoNoLover() throws Exception {
        // 准备数据：创建一个没有绑定伴侣的用户
        User user = User.builder()
                .userName("nolovers")
                .password("123456")
                .nickName("无伴侣用户")
                .build();
        userMapper.insert(user);

        // 生成令牌
        String token = jwtUtil.generateToken(user.getId());

        // 调用接口并验证异常
        mockMvc.perform(MockMvcRequestBuilders.get("/users/lover")
                        .header("Authorization", "Bearer " + token))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("GetLoverInfo no lover response: {}", responseContent);
                    assertTrue(responseContent.contains("当前用户还没有绑定伴侣"));
                })
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}