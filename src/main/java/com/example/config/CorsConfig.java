package com.example.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {



    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ⭐ 允许哪些前端地址访问（生产环境改成你真实的域名）
        config.setAllowedOriginPatterns(Arrays.asList("*"));  // 开发环境用 *，生产环境不要这样！
        // 生产环境应该这样写：
        // config.setAllowedOrigins(Arrays.asList("https://你的域名.com"));

        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带凭证（Cookie）
        config.setAllowCredentials(true);

        // 预检请求缓存时间（1小时，减少 OPTIONS 请求次数）
        config.setMaxAge(3600L);

        // 暴露哪些响应头给前端（如果你需要前端读取自定义头）
        config.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
