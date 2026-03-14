package com.kmbeast.config;

import com.kmbeast.mapper.PetMapper;
import org.redisson.api.RBloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时加载宠物ID到布隆过滤器
 */
@Component
public class BloomFilterLoader implements InitializingBean {

    // 日志对象（SLF4J）
    private static final Logger log = LoggerFactory.getLogger(BloomFilterLoader.class);

    @Autowired
    private RBloomFilter<Integer> petIdBloomFilter;

    @Autowired
    private PetMapper petMapper;

    @Override
    public void afterPropertiesSet(){
        // 前置校验：避免空指针
        if (petMapper == null) {
            log.error("PetMapper 注入失败，无法加载宠物ID到布隆过滤器");
            return;
        }
        if (petIdBloomFilter == null) {
            log.error("petIdBloomFilter 注入失败，无法加载宠物ID到布隆过滤器");
            return;
        }
        try {
            // 从数据库查询所有宠物ID
            List<Integer> allPetIds = petMapper.queryAllIds();
            log.info("开始加载宠物ID到布隆过滤器，总数：{}", allPetIds.size());

            // 批量添加ID到布隆过滤器
            for (Integer id : allPetIds) {
                petIdBloomFilter.add(id);
            }

            log.info("布隆过滤器加载完成，共加载 {} 个宠物ID", allPetIds.size());
        } catch (Exception e) {
            // 捕获异常并打印，避免启动失败
            log.error("加载宠物ID到布隆过滤器失败", e);
        }
    }
}
