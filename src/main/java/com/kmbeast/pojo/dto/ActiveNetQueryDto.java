package com.kmbeast.pojo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kmbeast.common.json.LenientIntegerDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *行为互动查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ActiveNetQueryDto extends QueryDto{
    /**
     * 用户ID，外键，关联的是用户表
     */
    @JsonDeserialize(using = LenientIntegerDeserializer.class)
    private Integer userId;
    /**
     * 内容ID，与内容模块配合使用
     */
    @JsonDeserialize(using = LenientIntegerDeserializer.class)
    private Integer contentId;
    /**
     * 内容模块
     */
    private String contentType;
    /**
     * 行为类型（1：浏览；2：点赞；3：收藏）
     */
    @JsonDeserialize(using = LenientIntegerDeserializer.class)
    private Integer type;
    /**
     * 查询天数
     */
    @JsonDeserialize(using = LenientIntegerDeserializer.class)
    private Integer days;
}