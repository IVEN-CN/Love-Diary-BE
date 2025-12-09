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
}
