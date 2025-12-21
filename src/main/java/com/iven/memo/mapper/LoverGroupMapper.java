package com.iven.memo.mapper;

import com.iven.memo.models.DO.LoverGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface LoverGroupMapper {
    /**
     * 插入伴侣组
     * @param loverGroup 伴侣组信息，其id字段可以为null
     */
    void insert(LoverGroup loverGroup);

    /**
     * 通过id更新
     * @param loverGroup 新的伴侣组信息，其id字段不能为null
     */
    void updateById(LoverGroup loverGroup);

    /**
     * 通过id删除
     * @param id 伴侣组id
     */
    void deleteById(Long id);

    /**
     * 通过伴侣组id查找
     * @param id 伴侣组id
     * @return 对应的伴侣组信息
     */
    Optional<LoverGroup> findById(Long id);

    /**
     * 通过伴侣组合的成员id查找
     * @param id 成员Id
     * @return 对应的伴侣组信息
     */
    Optional<LoverGroup> findByLoverId(Long id);

    /**
     * 通过伴侣的id删除伴侣组，用于伴侣解绑
     * @param id 成员id
     */
    void deleteByLoverId(Long id);
}
