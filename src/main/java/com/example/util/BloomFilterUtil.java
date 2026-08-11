package com.example.util;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BloomFilterUtil {

    @Autowired
    private RedissonClient redissonClient;

    // 定义布隆过滤器的名字（Redis 中存的 Key）
    private static final String BLOOM_FILTER_NAME = "bloom:usernames";

    // 布隆过滤器对象（单例，只初始化一次）
    private RBloomFilter<String> bloomFilter;

    /**
     * 项目启动时，自动初始化布隆过滤器
     * 相当于在洗衣店开门前，先把小黑板准备好
     */
    @PostConstruct
    public void init() {
        // 获取或创建布隆过滤器
        // 参数1：过滤器名字
        // 参数2：预期插入的数据量（预计未来有 100 万用户）
        // 参数3：误判率（1% 的误判率）
        // 解释：1000000L 代表预计放 100 万件衣服进仓库，误判率 0.01（即 1%）
        // 注意：这个参数决定了占用内存大小，不能随便填！
        // 如果预计 1000 万，就填 10000000L
        bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
        bloomFilter.tryInit(1000000L, 0.01);
    }

    /**
     * 新增用户名到布隆过滤器（注册时调用）
     */
    public void addUsername(String username) {
        if (username != null && !username.isEmpty()) {
            bloomFilter.add(username);
        }
    }

    /**
     * 检查用户名是否可能存在（登录/注册时校验）
     *
     * @return true = 可能存在（需要去查 DB 确认）
     *         false = 绝对不存在（可以直接拒绝，不用查 DB）
     */
    public boolean mightContainUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return bloomFilter.contains(username);
    }
}
