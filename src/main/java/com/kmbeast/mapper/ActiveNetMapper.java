package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.ActiveNetQueryDto;
import com.kmbeast.pojo.entity.ActiveNet;

import java.util.List;

/**
 *行为互动持久化接口
 */

public interface ActiveNetMapper extends BaseMapper<ActiveNet> {

    /**
     * 查询行为互动信息
     *
     * @param activeNetQueryDto 查询条件类
     * @return List<ActiveNet>
     */
    List<ActiveNet> query(ActiveNetQueryDto activeNetQueryDto);

    /**
     * 查询符合条件的记录数 - 配合前端做分页
     * @param activeNetQueryDto 查询条件类
     * @return Integer 记录数
     */
    Integer queryCount(ActiveNetQueryDto activeNetQueryDto);
}
