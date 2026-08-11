package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User selectUserById(Long id);

    User selectUserByUsername(String username);

    Integer insertUser(User user);

    Integer updateUser(User user);
}
