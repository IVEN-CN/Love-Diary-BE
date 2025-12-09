package com.iven.memo.controller;

import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.MemoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/memos")
public class MemoController {
    private final MemoService memoService;

    /**
     * 获取所有备忘录
     * @return 备忘录列表
     */
    @GetMapping
    public ResponseMessage<List<MemoInfoDTO>> getAllMemos() {
        return ResponseMessage.success(memoService.findAll());
    }

    /**
     * 添加新的备忘
     * @param memoInfoDTO 备忘信息DTO
     * @return 响应消息
     */
    @PostMapping
    public ResponseMessage<Void> addMemo(@RequestBody @Valid MemoInfoDTO memoInfoDTO) {
        memoService.add(memoInfoDTO);
        return ResponseMessage.success();
    }
}
