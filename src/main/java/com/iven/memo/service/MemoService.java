package com.iven.memo.service;

import com.iven.memo.models.DTO.Memo.MemoInfoDTO;
import com.iven.memo.models.DTO.Memo.MemoInfoWithIdDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface MemoService {
    /**
     * 获取所有备忘录
     * @return 备忘录列表
     */
    List<MemoInfoWithIdDTO> findAll();

    /**
     * 添加新的备忘录
     * @param memoInfoDTO 备忘录信息DTO
     */
    void add(MemoInfoDTO memoInfoDTO);

    /**
     * 根据id删除备注
     * @param id id
     */
    void delete(Long id);

    /**
     * 更新备忘录
     * @param id 备忘录id
     * @param memoInfoDTO 备忘录信息DTO
     */
    void update(Long id, MemoInfoDTO memoInfoDTO);
}
