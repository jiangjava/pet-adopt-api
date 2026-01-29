package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.ActiveNetMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.ActiveNetQueryDto;
import com.kmbeast.pojo.entity.ActiveNet;
import com.kmbeast.service.ActiveNetService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为互动业务逻辑接口实现类
 */
@Service
public class ActiveNetServiceImpl extends ServiceImpl<ActiveNetMapper,ActiveNet> implements ActiveNetService {

    /**
     * 查询行为互动信息
     *
     * @param activeNetQueryDto 查询条件类
     * @return Result<List<ActiveNet>> 后台通用返回封装类
     */
    @Override
    public Result<List<ActiveNet>> query(ActiveNetQueryDto activeNetQueryDto) {
        List<ActiveNet> activeNetList = this.getBaseMapper().query(activeNetQueryDto);
        Integer count = this.getBaseMapper().queryCount(activeNetQueryDto);
        return ApiResult.success(activeNetList, count);
    }


    /**
     * 行为互动新增
     * @param activeNet 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(ActiveNet activeNet) {
        System.out.println("--------校验开始-------");
        AssertUtils.notNull(activeNet.getContentId(),"内容id不为空");
        AssertUtils.notNull(activeNet.getType(),"互动类型不为空");
        AssertUtils.notNull(activeNet.getContentId(),"内容类型不为空");
        activeNet.setUserId(LocalThreadHolder.getUserId());//设置当前操作者用户ID
        activeNet.setCreateTime(LocalDateTime.now());//设置行为互动时间
        System.out.println("--------校验结束-------");
        save(activeNet);// 调用Mybatis-plus提供的新增方法
        return ApiResult.success();
    }
}
