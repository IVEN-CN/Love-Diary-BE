package com.iven.memo.controller;

import com.iven.memo.BaseTest;
import com.iven.memo.LoginTest;
import com.iven.memo.mapper.LoverGroupMapper;
import com.iven.memo.mapper.MemoMapper;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.LoverGroup;
import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.Enumerate.MemoType;
import com.iven.memo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class MemoControllerTest extends BaseTest implements LoginTest {

    @Autowired
    private MemoMapper memoMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoverGroupMapper loverGroupMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private String jwt = "";

    private String getAuthHeaderContent() {
        return "Bearer " + jwt;
    }

    @Override
    @BeforeEach
    public void LoginBeforeTest() {
        // 插入user
        User user = User.builder()
                .nickName("test_user_nick_name")
                .userName("test_user_name")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(user);
        log.info("mybatis insert user in LoginBeforeTest: {}", user);

        // 生成jwt
        jwt = jwtUtil.generateToken(user.getId());
    }

    /**
     * <h1>获取备忘条例</h1>
     */
    @Test
    void testGetMemos() throws Exception {
        // 数据准备
        Memo memo1 = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("测试文字")
                .userId(1L)
                .build();
        memoMapper.insert(memo1);
        log.info("saved memo in testGetMemos: {}", memo1);

        Memo memo2 = Memo.builder()
                .type(MemoType.BAD_EVENT)
                .date(LocalDate.now().plusDays(1))
                .details("测试文字2")
                .userId(1L)
                .build();
        memoMapper.insert(memo2);
        log.info("saved memo in testGetMemos: {}", memo2);

        // 执行测试
        mockMvc.perform(MockMvcRequestBuilders.get("/memos")
                        .header("Authorization", getAuthHeaderContent())
                ).andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testGetMemos: {}", responseContent);
                }).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * <h1>添加备忘条例</h1>
     */
    @Test
    void testAddMemo() throws Exception {
        // 准备请求体
        MemoInfoDTO memoInfoDTO = new MemoInfoDTO(LocalDate.now(), MemoType.NICE_EVENT, "这是一个测试备忘录");
        String requestBody = objectMapper.writeValueAsString(memoInfoDTO);
        log.info("Request body for testAddMemo: {}", requestBody);

        // 执行操作
        mockMvc.perform(MockMvcRequestBuilders.post("/memos")
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testAddMemo: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 数据库校验
        List<Memo> memos = memoMapper.findAll();
        log.info("Memos in database after testAddMemo: {}", memos);
        Assertions.assertEquals(1, memos.size());
    }

    /**
     * <h1>添加备忘条例，缺少日期字段</h1>
     */
    @Test
    void testAddMemoMissDate() throws Exception {
        // 准备请求体
        MemoInfoDTO memoInfoDTO = new MemoInfoDTO(null, MemoType.NICE_EVENT, "这是一个测试备忘录");
        String requestBody = objectMapper.writeValueAsString(memoInfoDTO);
        log.info("Request body for testAddMemoMissDate: {}", requestBody);

        // 执行操作
        mockMvc.perform(MockMvcRequestBuilders.post("/memos")
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testAddMemoMissDate: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // 数据库校验
        List<Memo> memos = memoMapper.findAll();
        log.info("Memos in database after testAddMemoMissDate: {}", memos);
        Assertions.assertEquals(0, memos.size());
    }

    /**
     * <h1>添加备忘条例，缺少类型字段</h1>
     */
    @Test
    void testAddMemoMissType() throws Exception {
        // 准备请求体
        MemoInfoDTO memoInfoDTO = new MemoInfoDTO(LocalDate.now(), null, "这是一个测试备忘录");
        String requestBody = objectMapper.writeValueAsString(memoInfoDTO);
        log.info("Request body for testAddMemoMissType: {}", requestBody);

        // 执行操作
        mockMvc.perform(MockMvcRequestBuilders.post("/memos")
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testAddMemoMissType: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // 数据库校验
        List<Memo> memos = memoMapper.findAll();
        log.info("Memos in database after testAddMemoMissType: {}", memos);
        Assertions.assertEquals(0, memos.size());
    }

    /**
     * <h1>修改备忘</h1>
     * 正常修改
     */
    @Test
    void testUpdateMemo() throws Exception {
        // 数据准备
        Memo memo = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("原始测试文字")
                .userId(1L)
                .build();
        memoMapper.insert(memo);
        log.info("Original memo for testUpdateMemo: {}", memo);

        // 准备请求体
        MemoInfoDTO updatedMemoInfoDTO = new MemoInfoDTO(
                LocalDate.now().plusDays(5),
                MemoType.BAD_EVENT,
                "修改后的测试文字"
        );
        String requestBody = objectMapper.writeValueAsString(updatedMemoInfoDTO);
        log.info("Request body for testUpdateMemo: {}", requestBody);

        // 执行更新
        mockMvc.perform(MockMvcRequestBuilders.put("/memos/{id}", memo.getId())
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testUpdateMemo: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 校验结果
        Memo updatedMemo = memoMapper.findById(memo.getId());
        Assertions.assertNotNull(updatedMemo);
        Assertions.assertEquals(MemoType.BAD_EVENT, updatedMemo.getType());
        Assertions.assertEquals("修改后的测试文字", updatedMemo.getDetails());
        Assertions.assertEquals(LocalDate.now().plusDays(5), updatedMemo.getDate());
    }

    /**
     * <h1>修改备忘</h1>
     * 参数缺失(400)
     */
    @Test
    void testUpdateMemoMissingParams() throws Exception {
        // 数据准备
        Memo memo = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("原始测试文字")
                .userId(1L)
                .build();
        memoMapper.insert(memo);
        log.info("Original memo for testUpdateMemoMissingParams: {}", memo);

        // 准备请求体，缺少字段
        MemoInfoDTO invalidMemoInfoDTO = new MemoInfoDTO(null, null, null);
        String requestBody = objectMapper.writeValueAsString(invalidMemoInfoDTO);
        log.info("Request body for testUpdateMemoMissingParams: {}", requestBody);

        // 执行更新
        mockMvc.perform(MockMvcRequestBuilders.put("/memos/{id}", memo.getId())
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testUpdateMemoMissingParams: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * <h1>修改备忘</h1>
     * 目标id不存在(404)
     */
    @Test
    void testUpdateMemoNonExistentId() throws Exception {
        // 准备请求体
        MemoInfoDTO updatedMemoInfoDTO = new MemoInfoDTO(
                LocalDate.now().plusDays(3),
                MemoType.NICE_EVENT,
                "更新内容"
        );
        String requestBody = objectMapper.writeValueAsString(updatedMemoInfoDTO);
        log.info("Request body for testUpdateMemoNonExistentId: {}", requestBody);

        // 执行更新
        mockMvc.perform(MockMvcRequestBuilders.put("/memos/{id}", -1L)
                        .header("Authorization", getAuthHeaderContent())
                        .contentType("application/json")
                        .content(requestBody))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testUpdateMemoNonExistentId: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    /**
     * <h1>删除备忘</h1>
     * 正常删除
     */
    @Test
    void testDeleteMemo() throws Exception {
        // 数据准备
        Memo memo = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("测试文字")
                .userId(1L)
                .build();
        memoMapper.insert(memo);
        log.info("Original memo for testDeleteMemo: {}", memo);

        // 执行删除
        mockMvc.perform(MockMvcRequestBuilders.delete("/memos/{id}", memo.getId())
                        .header("Authorization", getAuthHeaderContent())
                ).andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testDeleteMemo: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 校验删除
        Memo deletedMemo = memoMapper.findById(memo.getId());
        Assertions.assertNull(deletedMemo);
    }

    /**
     * <h1>删除备忘</h1>
     * 目标id不存在(200，幂等)
     */
    @Test
    void testDeleteMemoNonExistentId() throws Exception {
        // 执行删除
        mockMvc.perform(MockMvcRequestBuilders.delete("/memos/{id}", -1L)
                        .header("Authorization", getAuthHeaderContent()))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testDeleteMemoNonExistentId: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * <h1>多用户场景：不同LoverGroup的用户备忘互不可见</h1>
     * 测试用户A和用户B不属于同一个LoverGroup时，A无法看到B的备忘，B也无法看到A的备忘
     */
    @Test
    void testMemosNotVisibleBetweenDifferentLoverGroups() throws Exception {
        // 准备用户A
        User userA = User.builder()
                .nickName("测试用户A昵称")
                .userName("test_user_a")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userA);
        log.info("Created userA: {}", userA);

        // 准备用户B
        User userB = User.builder()
                .nickName("测试用户B昵称")
                .userName("test_user_b")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userB);
        log.info("Created userB: {}", userB);

        // 准备用户C（与用户A在同一个LoverGroup）
        User userC = User.builder()
                .nickName("测试用户C昵称")
                .userName("test_user_c")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userC);
        log.info("Created userC: {}", userC);

        // 创建LoverGroup1：用户A和用户C
        LoverGroup loverGroup1 = LoverGroup.builder()
                .user1Id(userA.getId())
                .user2Id(userC.getId())
                .createTime(LocalDate.now())
                .build();
        loverGroupMapper.insert(loverGroup1);
        log.info("Created loverGroup1 for userA and userC: {}", loverGroup1);

        // 用户B单独一人，不属于任何LoverGroup
        
        // 用户A创建备忘
        Memo memoA = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("用户A的私密备忘")
                .userId(userA.getId())
                .build();
        memoMapper.insert(memoA);
        log.info("Created memoA: {}", memoA);

        // 用户B创建备忘
        Memo memoB = Memo.builder()
                .type(MemoType.BAD_EVENT)
                .date(LocalDate.now().plusDays(1))
                .details("用户B的私密备忘")
                .userId(userB.getId())
                .build();
        memoMapper.insert(memoB);
        log.info("Created memoB: {}", memoB);

        // 生成用户A的JWT
        String jwtA = jwtUtil.generateToken(userA.getId());
        
        // 生成用户B的JWT
        String jwtB = jwtUtil.generateToken(userB.getId());

        // 用户A获取备忘列表，应该只能看到自己的备忘（不能看到用户B的）
        mockMvc.perform(MockMvcRequestBuilders.get("/memos")
                        .header("Authorization", "Bearer " + jwtA))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UserA get memos response: {}", responseContent);
                    // 验证响应中不包含用户B的备忘内容
                    Assertions.assertFalse(responseContent.contains("用户B的私密备忘"),
                            "用户A不应该看到用户B的备忘");
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 用户B获取备忘列表，应该只能看到自己的备忘（不能看到用户A的）
        mockMvc.perform(MockMvcRequestBuilders.get("/memos")
                        .header("Authorization", "Bearer " + jwtB))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UserB get memos response: {}", responseContent);
                    // 验证响应中不包含用户A的备忘内容
                    Assertions.assertFalse(responseContent.contains("用户A的私密备忘"),
                            "用户B不应该看到用户A的备忘");
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * <h1>多用户场景：同一LoverGroup的用户可以共享备忘</h1>
     * 测试用户A和用户B属于同一个LoverGroup时，A可以看到B的备忘，B也可以看到A的备忘
     */
    @Test
    void testMemosVisibleWithinSameLoverGroup() throws Exception {
        // 准备用户A
        User userA = User.builder()
                .nickName("恋人A昵称")
                .userName("lover_a")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userA);
        log.info("Created userA: {}", userA);

        // 准备用户B
        User userB = User.builder()
                .nickName("恋人B昵称")
                .userName("lover_b")
                .password("123456")
                .birthday(LocalDate.now())
                .build();
        userMapper.insert(userB);
        log.info("Created userB: {}", userB);

        // 创建LoverGroup：用户A和用户B
        LoverGroup loverGroup = LoverGroup.builder()
                .user1Id(userA.getId())
                .user2Id(userB.getId())
                .createTime(LocalDate.now())
                .build();
        loverGroupMapper.insert(loverGroup);
        log.info("Created loverGroup for userA and userB: {}", loverGroup);

        // 用户A创建备忘
        Memo memoA = Memo.builder()
                .type(MemoType.NICE_EVENT)
                .date(LocalDate.now())
                .details("用户A创建的共享备忘")
                .userId(userA.getId())
                .build();
        memoMapper.insert(memoA);
        log.info("Created memoA: {}", memoA);

        // 用户B创建备忘
        Memo memoB = Memo.builder()
                .type(MemoType.BAD_EVENT)
                .date(LocalDate.now().plusDays(2))
                .details("用户B创建的共享备忘")
                .userId(userB.getId())
                .build();
        memoMapper.insert(memoB);
        log.info("Created memoB: {}", memoB);

        // 生成用户A的JWT
        String jwtA = jwtUtil.generateToken(userA.getId());
        
        // 生成用户B的JWT
        String jwtB = jwtUtil.generateToken(userB.getId());

        // 用户A获取备忘列表，应该能看到自己的备忘和用户B的备忘
        mockMvc.perform(MockMvcRequestBuilders.get("/memos")
                        .header("Authorization", "Bearer " + jwtA))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UserA get memos response: {}", responseContent);
                    // 验证响应中包含用户A的备忘
                    Assertions.assertTrue(responseContent.contains("用户A创建的共享备忘"),
                            "用户A应该看到自己的备忘");
                    // 验证响应中包含用户B的备忘
                    Assertions.assertTrue(responseContent.contains("用户B创建的共享备忘"),
                            "用户A应该看到同一LoverGroup中用户B的备忘");
                })
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 用户B获取备忘列表，应该能看到自己的备忘和用户A的备忘
        mockMvc.perform(MockMvcRequestBuilders.get("/memos")
                        .header("Authorization", "Bearer " + jwtB))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("UserB get memos response: {}", responseContent);
                    // 验证响应中包含用户B的备忘
                    Assertions.assertTrue(responseContent.contains("用户B创建的共享备忘"),
                            "用户B应该看到自己的备忘");
                    // 验证响应中包含用户A的备忘
                    Assertions.assertTrue(responseContent.contains("用户A创建的共享备忘"),
                            "用户B应该看到同一LoverGroup中用户A的备忘");
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
