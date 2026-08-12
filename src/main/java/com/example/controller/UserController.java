package com.example.controller;

import com.example.common.Result;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.vo.LoginVO;
import com.example.vo.RegisterVO;
import com.example.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody User user) {
        RegisterVO registerVO = userService.register(user);
        return Result.success(registerVO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        LoginVO loginVO = userService.login(loginRequest, request);
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        /*
        * /users/logout 在 SecurityConfig 中配置为需要认证，
        * 因此未携带 Token 的请求根本不会到达 Controller，
        * 会直接由 JwtAuthenticationEntryPoint 返回 401
        * */
        /*if(authorization == null || !authorization.startsWith("Bearer ")){
            return Result.error("未登录");
        }*/
        String token = authorization.substring(7);
        userService.logout(token);
        return Result.success();
    }

    @PostMapping("/refreshToken")
    public Result<LoginVO> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginVO loginVO = userService.refreshToken(refreshToken);
        return Result.success(loginVO);
    }

    @GetMapping("/findUserById")
    public Result<UserVO> findUserById(@RequestParam
                                       @Min(value = 1, message = "用户 ID 必须大于 0")
                                       Long id) {
        UserVO userVO = userService.findUserById(id);
        return Result.success(userVO);
    }

    @GetMapping("/findUserByUsername")
    public Result<UserVO> findUserByUsername(@RequestParam
                                             @NotBlank(message = "用户名不能为空")  // ← 加上校验
                                             @Size(min = 4, max = 20, message = "用户名长度必须在 4-20 个字符之间")
                                             @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
                                             String username) {
        UserVO userVO = userService.findUserByUsername(username);
        return Result.success(userVO);
    }

}
