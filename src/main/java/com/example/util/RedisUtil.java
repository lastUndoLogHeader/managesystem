package com.example.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 作用：专门用来操作 RefreshToken 的存储和删除
 *
 * 为什么要把 RefreshToken 存到 Redis？
 * 1. 可以主动撤销（用户退出登录时删除）
 * 2. 可以设置过期时间（自动清理）
 * 3. 支持分布式（多台服务器共享）
 */
@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 存储 RefreshToken
     *
     * Key 的格式：refresh_token:{userId}
     * Value：RefreshToken 字符串
     *
     * 过期时间：30 天（和 RefreshToken 有效期一致）
     */
    public void saveRefreshToken(Long userId, String refreshToken, long expireSeconds) {
        String key = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expireSeconds, TimeUnit.SECONDS);
    }

    public void addToBlackList(Long userId, String accessToken, long expireSeconds){
        String tokenHash = getTokenHash(accessToken);
        String key = "blacklist:" + tokenHash;
        redisTemplate.opsForValue().set(key, accessToken, expireSeconds, TimeUnit.SECONDS);
        log.info("Token 已加入黑名单，用户 ID：{}，过期时间：{} 秒", userId, expireSeconds);
    }

    public boolean isTokenInBlackList(String accessToken){
        String key = "blacklist:" + getTokenHash(accessToken);
        return redisTemplate.hasKey(key);
    }

    /**
     * 计算 Token 的 MD5 哈希值（转为 32 位小写字符串）
     *
     * 为什么不用 UUID？因为同样的 Token 必须生成同样的哈希值。
     * MD5 保证：同样的输入 → 同样的输出。
     */
    private String getTokenHash(String token) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // 把字节数组转成 16 进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // 不会发生（MD5 是 Java 自带算法）
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    /**
     * 获取 RefreshToken
     *
     * 如果返回 null，说明该用户的 RefreshToken 不存在或已过期
     */
    public String getRefreshToken(Long userId) {
        String key = "refresh_token:" + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除 RefreshToken（用户退出登录时调用）
     */
    public void deleteRefreshToken(Long userId) {
        String key = "refresh_token:" + userId;
        redisTemplate.delete(key);
    }

    /**
     * 检查 RefreshToken 是否有效
     *
     * 验证逻辑：Redis 里存在，且和传入的 token 一致
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
