package com.iven.memo.mapper;

import com.iven.memo.models.DO.BindInvite;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BindInviteMapper {
    void insert(BindInvite invite);
    Optional<BindInvite> findById(Long id);
    void deleteById(Long id);
    void useInvite(Long id);
    List<BindInvite> findAll();
}
