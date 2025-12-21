package com.iven.memo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iven.memo.BaseTest;
import com.iven.memo.LoginTest;
import com.iven.memo.mapper.LoverGroupMapper;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.LoverGroup;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.Achievement.AchievementDisplayDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AchievementControllerTest extends BaseTest implements LoginTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoverGroupMapper loverGroupMapper;
    private String jwt;
    private User user1;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGetAchievementsFromUserWithLover() throws Exception {
        // 数据准备
        // 准备user1的伴侣
        User user2 = User.builder()
                .userName("test_user2")
                .password("123456")
                .build();
        userMapper.insert(user2);
        log.info("mybatis insert user2 {}", user2);

        // 插入情侣组
        LoverGroup loverGroup = LoverGroup.builder()
                .user1Id(user1.getId())
                .user2Id(user2.getId())
                .createTime(LocalDate.now().minusDays(20))
                .build();
        loverGroupMapper.insert(loverGroup);
        log.info("mybatis insert loverGroup {}", loverGroup);

        // 操作
        mockMvc.perform(
                MockMvcRequestBuilders.get("/achievements")
                        .header("Authorization", "Bearer " + jwt))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testGetAchievementsFromUserWithLover: {}", responseContent);

                    // 转换为DTO
                    ResponseMessage<?> responseMessage = objectMapper.readValue(responseContent, ResponseMessage.class);
                    // 更安全的强转（避免类型转换异常）
                    List<AchievementDisplayDTO> displayDTOs;
                    if (responseMessage.getDetails() instanceof List<?>) {
                        displayDTOs = ((List<?>) responseMessage.getDetails()).stream()
                                .map(achievement -> objectMapper.convertValue(achievement, AchievementDisplayDTO.class))
                                .toList();
                    } else {
                        // 处理类型不匹配的情况，比如返回空列表
                        displayDTOs = List.of();
                    }
                    Assertions.assertNotNull(displayDTOs);
                }).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testGetAchievementFromUserWithoutLover() throws Exception {
        // 直接操作
        mockMvc.perform(
                MockMvcRequestBuilders.get("/achievements")
                        .header("Authorization", "Bearer " + jwt)
        ).andDo(result -> {
            String responseContent = result.getResponse().getContentAsString();
            log.info("Response content in testGetAchievementFromUserWithoutLover: {}", responseContent);
            ResponseMessage<?> responseMessage = objectMapper.readValue(responseContent, ResponseMessage.class);
            Assertions.assertTrue(((List<?>)responseMessage.getDetails()).isEmpty());        // 返回的成就列表应该是空的
        }).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Override
    @BeforeEach
    public void LoginBeforeTest() {
        user1 = User.builder()
                .userName("test_user1")
                .password("123456")
                .build();
        userMapper.insert(user1);
        log.info("mybatis insert user1 {}", user1);

        jwt = jwtUtil.generateToken(user1.getId());
    }
}