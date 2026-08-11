package com.example.service;

import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.vo.LoginVO;
import com.example.vo.RegisterVO;
import com.example.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService {

    UserVO findUserById(Long id);

    UserVO findUserByUsername(String username);

    RegisterVO register(User user);

    LoginVO login(LoginRequest loginRequest, HttpServletRequest request);

    LoginVO refreshToken(String token);

    void logout(String token);
}
