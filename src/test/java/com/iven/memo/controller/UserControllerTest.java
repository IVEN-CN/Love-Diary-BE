package com.iven.memo.controller;

import com.iven.memo.BaseTest;
import com.iven.memo.handler.CommonMessageWebSocketHandler;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.User.*;
import com.iven.memo.models.Enumerate.WSMessageType;
import com.iven.memo.models.Message.LoverBindMessage;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.models.Message.WSMessage;
import com.iven.memo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
class UserControllerTest extends BaseTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @MockitoBean
    private CommonMessageWebSocketHandler commonMessageWebSocketHandler;

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

    /**
     * 测试完整的伴侣绑定流程：
     * 1. 用户A 发起邀请给 用户B
     * 2. 系统生成短链并通过WebSocket通知用户B（Mock捕获）
     * 3. 用户B 点击链接接受邀请
     * 4. 验证数据库中两人绑定关系建立
     */
    @Test
    void bindAndAcceptLoverFlow() throws Exception {
        // 1. 数据准备：创建两个单身用户
        User userA = User.builder()
                .userName("UserA_Invite")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        User userB = User.builder()
                .userName("UserB_Accept")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userA);
        userMapper.insert(userB);

        String tokenA = jwtUtil.generateToken(userA.getId());
        String tokenB = jwtUtil.generateToken(userB.getId());

        // 2. 用户A 发起绑定邀请
        OnlyUserNameDTO inviteDTO = new OnlyUserNameDTO();
        inviteDTO.setUsername(userB.getUserName());

        mockMvc.perform(MockMvcRequestBuilders.put("/users/lover")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inviteDTO)))
                .andDo(result -> log.info("Bind Invite response: {}", result.getResponse().getContentAsString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("邀请已发送"));

        // 3. 捕获 WebSocket 消息以获取生成的短链 (Link)
        // 注意：因为是异步事件监听，使用 timeout 等待
        ArgumentCaptor<WSMessage> wsMessageCaptor = ArgumentCaptor.forClass(WSMessage.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(commonMessageWebSocketHandler, timeout(2000)).sendMessage(userIdCaptor.capture(), wsMessageCaptor.capture());

        // 验证消息是发给 UserB 的
        assertEquals(userB.getId(), userIdCaptor.getValue());

        WSMessage<?> capturedMsg = wsMessageCaptor.getValue();
        assertEquals(WSMessageType.LOVER_BIND, capturedMsg.getMessageType());

        // 解析消息体获取 Link
        //以此处实际序列化行为为准，通常Mock捕获的可能是Map或具体对象，这里利用ObjectMapper转换确保类型安全
        LoverBindMessage bindMessage = objectMapper.convertValue(capturedMsg.getMessage(), LoverBindMessage.class);
        String inviteLink = bindMessage.getLink();
        assertNotNull(inviteLink, "邀请链接不应为空");
        log.info("Captured invite link: {}", inviteLink);

        // 4. 用户B 接受绑定邀请
        mockMvc.perform(MockMvcRequestBuilders.post("/users/lover/accept/" + inviteLink)
                        .header("Authorization", "Bearer " + tokenB))
                .andDo(result -> log.info("Accept Bind response: {}", result.getResponse().getContentAsString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("伴侣绑定成功"));

        // 5. 验证数据库状态
        User updatedA = userMapper.findById(userA.getId()).orElseThrow();
        User updatedB = userMapper.findById(userB.getId()).orElseThrow();

        assertEquals(updatedB.getId(), updatedA.getLoverId(), "用户A的伴侣应为用户B");
        assertEquals(updatedA.getId(), updatedB.getLoverId(), "用户B的伴侣应为用户A");
    }

    /**
     * 测试伴侣解绑功能
     */
    @Test
    void deBindLover() throws Exception {
        // 1. 数据准备：创建一对已绑定的伴侣
        User userA = User.builder()
                .userName("UserA_Bound")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        User userB = User.builder()
                .userName("UserB_Bound")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userA);
        userMapper.insert(userB);

        // 手动建立数据库关联
        userA.setLoverId(userB.getId());
        userB.setLoverId(userA.getId());
        userMapper.updateById(userA);
        userMapper.updateById(userB);

        String tokenA = jwtUtil.generateToken(userA.getId());

        // 2. 用户A 发起解绑
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/lover")
                        .header("Authorization", "Bearer " + tokenA))
                .andDo(result -> log.info("Debind response: {}", result.getResponse().getContentAsString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("伴侣解绑成功"));

        // 3. 验证数据库状态
        User updatedA = userMapper.findById(userA.getId()).orElseThrow();
        User updatedB = userMapper.findById(userB.getId()).orElseThrow();

        assertNull(updatedA.getLoverId(), "用户A应当没有伴侣");
        assertNull(updatedB.getLoverId(), "用户B应当没有伴侣");

        // 4. 验证是否发送了解绑通知给对方 (异步)
        verify(commonMessageWebSocketHandler, timeout(2000).atLeastOnce())
                .sendMessage(eq(userB.getId()), any(WSMessage.class));
    }
}