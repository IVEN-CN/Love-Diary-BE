package com.iven.memo.mapper;

import com.iven.memo.models.DO.Memo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemoMapper {
    /**
     * 根据ID查找备忘录
     * @param id 备忘录ID
     * @return 备忘录对象
     */
    Memo findById(Long id);

    /**
     * 查找所有备忘录
     * @return 备忘录列表
     */
    List<Memo> findAll();

    /**
     * 根据用户ID查找备忘录
     * @param userId 用户ID
     * @return 备忘录列表
     */
    List<Memo> findByUserId(Long userId);

    /**
     * 插入新的备忘录
     * @param memo 备忘录对象
     */
    void insert(Memo memo);

    /**
     * 更新备忘录
     *
     * @param memo 备忘录对象
     * @return
     */
    int update(Memo memo);

    /**
     * 根据ID删除备忘录
     * @param id 备忘录ID
     */
    void deleteById(Long id);
}
