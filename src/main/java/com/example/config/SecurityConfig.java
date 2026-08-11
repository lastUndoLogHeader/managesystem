package com.example.config;

import com.example.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF 防护（开发阶段先关掉）
                .csrf(csrf -> csrf.disable())

                // 2. 设置 Session 策略为无状态（因为我们是 JWT，不用 Session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ⭐ 配置异常处理，使用自定义认证入口点
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 3. 配置哪些接口需要认证，哪些不需要
                .authorizeHttpRequests(auth -> auth
                        // 放行：注册和登录接口不需要 Token
                        .antMatchers("/users/register", "/users/login", "/users/refreshToken").permitAll()
                        // 其他所有请求都需要认证（需要 Token）
                        .anyRequest().authenticated()
                )

                // 4. 把我们自定义的 JWT 过滤器，放在 UsernamePasswordAuthenticationFilter 之前
                //    这样 Spring Security 在处理请求前，会先经过我们的过滤器，把 Token 解析成认证信息
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}