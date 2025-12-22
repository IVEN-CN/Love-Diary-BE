package com.iven.memo.mapper;

import com.iven.memo.models.DO.Memo;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemoMapper {
    /**
     * 根据ID查找备忘录
     * @param id 备忘录ID
     * @return 备忘录对象
     */
    Optional<Memo> findById(Long id);

    /**
     * 查找所有备忘录
     * @return 备忘录列表
     */
    List<Memo> findAll();

    /**
     * 根据拥有者ID查找备忘录
     *
     * @param userId  拥有者用户ID
     * @param loverId 伴侣id
     * @return 备忘录列表
     */
    List<Memo> findAllByOwnerAndLoverId(Long userId, @Nullable Long loverId);

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

    /**
     * 检查备忘录是否属于用户
     * @param ownerId 备用户ID
     * @param memoId 忘录ID
     * @return true 如果是则返回true，否则返回false
     */
    Boolean isOwner(Long ownerId, Long memoId);
}
