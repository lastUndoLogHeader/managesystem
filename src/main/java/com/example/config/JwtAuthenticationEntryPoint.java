package com.example.config;

// ----- 导入统一返回工具 -----
import com.example.common.Result;
import com.example.common.ResultCode;

// ----- 导入 JSON 转换工具 -----
import com.fasterxml.jackson.databind.ObjectMapper;

// ----- 导入 Spring Security 核心 -----
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 用来把 Java 对象转成 JSON 字符串的工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 这个方法什么时候被调用？
     * 当 Spring Security 发现“当前请求没有认证信息”时，就会自动调用这个方法。
     * 比如：
     * 1. 我们没带 Token（过滤器贴了 3004 标签）
     * 2. 带了过期 Token（过滤器贴了 3005 标签）
     * 3. 带了无效 Token（过滤器贴了 3006 标签）
     */
    @Override
    public void commence(HttpServletRequest request,          // 请求对象（可以取出我们贴的标签）
                         HttpServletResponse response,       // 响应对象（用来写 JSON 返回给前端）
                         AuthenticationException authException) // Spring Security 自己抛的认证异常（我们暂时不用）
            throws IOException {

        log.warn("认证失败，触发 AuthenticationEntryPoint（开始处理未认证请求）");

        // ---------- 第1步：从 request 中撕下我们贴的“标签” ----------
        // 在过滤器里，我们放了 request.setAttribute("tokenError", ResultCode.XXX)
        // 现在在这里把它取出来
        ResultCode errorCode = (ResultCode) request.getAttribute("tokenError");

        // ---------- 第2步：如果标签是空的（兜底逻辑） ----------
        // 理论上过滤器一定会贴标签，但万一因为某种原因没贴上，我们就默认返回 3004
        if (errorCode == null) {
            log.warn("request 中没有 tokenError 属性，默认返回 UNAUTHORIZED");
            errorCode = ResultCode.UNAUTHORIZED;
        }

        // ---------- 第3步：设置 HTTP 网络状态码 ----------
        // 不管业务错误是 3004、3005 还是 3006，网络通道状态永远返回 401（未认证）
        // 这一步是告诉浏览器/网关：“这个请求在通道层就被拦截了”
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 即 401

        // 告诉浏览器，我返回的是 JSON 格式，并且编码是 UTF-8（防止中文乱码）
        response.setContentType("application/json;charset=UTF-8");

        // ---------- 第4步：构建业务返回体（JSON 格式） ----------
        // 利用你 Result 类里的快捷方法 error(ResultCode)，传入取出来的枚举
        // 比如传入 ResultCode.TOKEN_EXPIRED，就会自动生成：
        // {"code": 3005, "msg": "登录已过期，请重新登录", "data": null}
        Result<Void> result = Result.error(errorCode);

        // ---------- 第5步：把 Java 对象转成 JSON 字符串，写到响应流里 ----------
        // objectMapper.writeValueAsString(result) 会把对象变成 {"code":3005,...}
        // response.getWriter().write() 把这个字符串写到网络上，返回给前端
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}