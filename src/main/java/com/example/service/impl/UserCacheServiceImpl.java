package com.example.service.impl;

import com.example.mapper.UserMapper;
import com.example.service.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserMapper userMapper;

    // 缓存 Key（固定不变）
    private static final String TOTAL_USER_KEY = "user:total:count";

    @Override
    public Long getNumOfUsers() {
        Long cachedNumsOfUsers = (Long)redisTemplate.opsForValue().get(TOTAL_USER_KEY);
        return null;
    }
}
