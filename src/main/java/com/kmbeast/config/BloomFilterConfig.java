package com.kmbeast.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 *  布隆过滤器配置类
 */
@Configuration
public class BloomFilterConfig {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 初始化宠物ID布隆过滤器
     */
    @Bean
    public RBloomFilter<Integer> petIdBloomFilter() {
        // 前置校验：RedissonClient不能为空
        if (redissonClient == null) {
            throw new RuntimeException("RedissonClient 未初始化，无法创建布隆过滤器");
        }

        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter("petIdBloomFilter");
        // 初始化：预期插入数量 100000，误判率 0.01
        boolean initSuccess = bloomFilter.tryInit(100000L, 0.01);
        if (initSuccess) {
            log.info("宠物ID布隆过滤器初始化成功");
        } else {
            log.info("宠物ID布隆过滤器已存在，无需重复初始化");
        }
        return bloomFilter;
    }
    // 补充：添加日志（如果需要在配置类中打印日志）
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BloomFilterConfig.class);
}
