package com.example.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 为每个请求生成唯一的 traceId
            String traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(TRACE_ID, traceId);

            // 也可以从请求头中获取（如果前端传了的话）
            String headerTraceId = request.getHeader("X-Trace-Id");
            if (headerTraceId != null && !headerTraceId.isEmpty()) {
                MDC.put(TRACE_ID, headerTraceId);
            }

//            log.info("请求开始：{} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
//            log.info("请求结束：{} {}", request.getMethod(), request.getRequestURI());
        } finally {
            // 请求结束后清除，防止内存泄漏
            MDC.clear();
        }
    }
}
