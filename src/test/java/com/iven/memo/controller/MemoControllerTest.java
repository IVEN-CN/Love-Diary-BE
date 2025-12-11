package com.iven.memo.controller;

import com.iven.memo.BaseTest;
import com.iven.memo.mapper.MemoMapper;
import com.iven.memo.models.DO.Memo;
import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.Enumerate.MemoType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
class MemoControllerTest extends BaseTest {

    @Autowired
    private MemoMapper memoMapper;

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
        mockMvc.perform(MockMvcRequestBuilders.get("/memos"))
                .andDo(result -> {
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
        mockMvc.perform(MockMvcRequestBuilders.delete("/memos/{id}", memo.getId()))
                .andDo(result -> {
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
        mockMvc.perform(MockMvcRequestBuilders.delete("/memos/{id}", -1L))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    log.info("Response content in testDeleteMemoNonExistentId: {}", responseContent);
                })
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
