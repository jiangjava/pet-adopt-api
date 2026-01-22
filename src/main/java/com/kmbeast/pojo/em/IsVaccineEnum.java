package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 宠物是否推荐状态枚举
 */
@Getter
@AllArgsConstructor
public enum IsVaccineEnum {

    No_ADOPT(false, "未接种"),
    ADOPT(true, "已接种");

    private final Boolean stats; // 状态
    private final String name; // 状态描述


}
