package com.example.filter;

// ----- 导入工具类 -----
import com.example.common.ResultCode;      // 我们的错误码枚举
import com.example.util.JwtUtils;          // 你的 JWT 解析工具类

// ----- 导入 JWT 解析时可能抛出的具体异常（关键！）-----
import com.example.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;         // 专门表示“Token 过期”的异常
import io.jsonwebtoken.MalformedJwtException;       // 专门表示“Token 格式错误”的异常
import io.jsonwebtoken.security.SignatureException; // 专门表示“Token 签名被篡改”的异常

// ----- 导入 Spring 相关 -----
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList; // 用来创建空的权限列表

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 这个方法是每个请求进来时，最先经过的地方（只执行一次）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,      // 请求对象（可以往里塞属性）
                                    HttpServletResponse response,   // 响应对象（暂时不用）
                                    FilterChain filterChain)        // 过滤器链（用来放行请求）
            throws ServletException, IOException {

        // ---------- 第1步：尝试从请求头里拿 Token ----------
        // 前端请求时，会在 Header 里放：Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
        String authHeader = request.getHeader("Authorization");

        // ---------- 第2步：判断是否带了 Token ----------
        // 如果 Header 是空的，或者不是以 "Bearer " 开头，说明根本没带 Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 🔴 情况1：没带 Token → 在 request 上贴标签：错误码是 3004（未登录）
            request.setAttribute("tokenError", ResultCode.UNAUTHORIZED);

            // ⚠️ 注意：这里必须放行！不能直接拦截。
            // 因为我们要让 Spring Security 的后续机制去调用 AuthenticationEntryPoint，
            // 如果在这里直接 return 或写响应，EntryPoint 就不会被触发了。
            filterChain.doFilter(request, response);
            return; // 放行后直接结束这个方法，不再往下执行
        }

        // ---------- 第3步：有 Token，但去掉前缀 "Bearer "，拿到纯 Token 字符串 ----------
        // 比如 "Bearer abc123" 变成 "abc123"
        String token = authHeader.substring(7);

        // ---------- 第4步：开始尝试解析 Token（这里最容易出异常） ----------
        try {
            // 调用你写的 JwtUtils 工具类，解析 Token，拿到里面的数据（用户名、过期时间等）
            Claims claims = jwtUtils.parseToken(token);

            if (redisUtil.isTokenInBlackList(token)) {
                log.warn("Token 已被拉黑（用户主动退出），用户：{}", claims.get("username", String.class));
                request.setAttribute("tokenError", ResultCode.TOKEN_INVALID);
                //token已经被拉黑，请求不应该被放行
                //filterChain.doFilter(request, response);
                return;
            }

            // 从解析出的数据里提取用户名（你当初存进去的）
            String username = claims.get("username", String.class);
            log.debug("Token 解析成功，用户：{}", username);

            // ---------- 第5步：解析成功，把用户信息存进 Spring Security 的“全局上下文” ----------
            // 这一步非常重要！告诉 Spring Security：“这个用户已经登录了，不用再拦截了”。
            // 构造一个 UserDetails 对象（Spring Security 要求的标准用户格式）
            UserDetails userDetails = new User(username, "", new ArrayList<>());

            // 构造一个“已认证令牌”，塞进上下文
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ Token 有效，正常放行（没有贴任何错误标签，因为没错误）
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 🟡 情况2：Token 过期 → 贴标签：错误码 3005
            // 注意：ExpiredJwtException 是专门针对“过期”的异常类型
            log.warn("Token 已过期：{}", e.getMessage());
            request.setAttribute("tokenError", ResultCode.TOKEN_EXPIRED);

            // 清空上下文（防止遗留脏数据）
            SecurityContextHolder.clearContext();
            // 放行，让 EntryPoint 去处理
            filterChain.doFilter(request, response);

        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            // 🔴 情况3：Token 无效 → 贴标签：错误码 3006
            // SignatureException：签名不对（被篡改）
            // MalformedJwtException：格式错误（不是标准的 JWT 字符串）
            // IllegalArgumentException：其他参数问题
            log.warn("Token 无效：{}", e.getMessage());
            request.setAttribute("tokenError", ResultCode.TOKEN_INVALID);

            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 🟠 终极兜底：其他未知异常（比如网络波动导致的解析失败），也当作无效 Token
            log.warn("Token 解析发生未知异常：{}", e.getMessage());
            request.setAttribute("tokenError", ResultCode.TOKEN_INVALID);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}