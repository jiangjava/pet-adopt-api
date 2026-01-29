package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.ActiveNetQueryDto;
import com.kmbeast.pojo.entity.ActiveNet;
import com.kmbeast.service.ActiveNetService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
* 行为互动控制器
* */

@RestController
@RequestMapping("/active-net")
public class ActiveNetController {

    @Resource
    private ActiveNetService activeNetService;

    /**
    * 行为互动新增
    * @param activeNet 实体数据
    * @return Result<String>通用返回封装类
    * */
    @ResponseBody
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody ActiveNet activeNet){
        return activeNetService.saveEntity(activeNet);
    }

    /**
     * 行为互动删除
     * @param id 主键ID
     * @return Result<String>通用返回封装类
     * */
    @ResponseBody
    @DeleteMapping(value = "/{id}")
    public Result<String> deleteById(@PathVariable Integer id){
        activeNetService.removeById(id);
        return ApiResult.success();
    }

    /**
     * 行为互动查询
     * @param activeNetQueryDto 查询条件类
     * @return Result<List<PetType>>通用返回封装类
     * */
    @Pager
    @ResponseBody
    @PostMapping(value = "/query")
    public Result<List<ActiveNet>> query(@RequestBody ActiveNetQueryDto activeNetQueryDto){
        return activeNetService.query(activeNetQueryDto);
    }


}

