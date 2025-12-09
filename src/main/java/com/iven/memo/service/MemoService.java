package com.iven.memo.service;

import com.iven.memo.models.DTO.Memo.MemoInfoDTO;

import java.util.List;

public interface MemoService {
    /**
     * 获取所有备忘录
     * @return 备忘录列表
     */
    List<MemoInfoDTO> findAll();

    /**
     * 添加新的备忘录
     * @param memoInfoDTO 备忘录信息DTO
     */
    void add(MemoInfoDTO memoInfoDTO);
}
