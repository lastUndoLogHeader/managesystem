package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    Long countAllUsers();

    User selectUserById(Long id);

    User selectUserByUsername(String username);

    Integer insertUser(User user);

    Integer updateUser(User user);
    
    List<User> selectUserByPage(@Param("pageSize") int pageSize, @Param("lastId") Long lastId);
}
