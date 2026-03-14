package com.kmbeast.pojo.entity;

import lombok.Data;

@Data
public class CacheMessage {
    private String cacheKey;      // 要删除的缓存 key
    private String operation;      // 预留：delete / update 等
    private Long timestamp;        // 时间戳，用于去重或日志
}
