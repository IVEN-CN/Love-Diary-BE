package com.iven.memo.mapper;

import com.iven.memo.models.DO.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findById(Long id);
    Optional<User> findByLoverId(Long loverId);
    Optional<User> findByNameAndPassword(String name, String password);
    void insert(User user);
    int updateById(User user);
    int updateByUsername(User user);
}
