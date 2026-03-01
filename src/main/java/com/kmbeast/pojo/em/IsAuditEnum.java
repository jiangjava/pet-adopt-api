package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
@AllArgsConstructor
public enum IsAuditEnum {

    NO_AUDIT(false, "未审核"),
    AUDIT(true, "已审核");

    private final Boolean status; // 领养状态
    private final String name; // 领养状态描述


}
