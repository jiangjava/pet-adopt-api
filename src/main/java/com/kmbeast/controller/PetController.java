package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.PetQueryDto;
import com.kmbeast.pojo.entity.Pet;
import com.kmbeast.pojo.vo.PetListItemVO;
import com.kmbeast.pojo.vo.PetVO;
import com.kmbeast.service.PetService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
* 宠物控制器
* */

@RestController
@RequestMapping("/pet")
public class PetController {

    @Resource
    private PetService petService;

    /**
    * 宠物新增
    * @param pet 数据
    * @return Result<String>通用返回封装类
    * */
    @ResponseBody
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody Pet pet){
        return petService.save(pet);
    }

    /**
     * 宠物修改
     * @param pet 主键ID
     * @return Result<String>通用返回封装类
     * */
    @ResponseBody
    @PutMapping(value = "/update")
    public Result<String> update(@RequestBody Pet pet){
        return petService.update(pet);
    }

    /**
     * 宠物删除
     * @param id 主键ID
     * @return Result<String>通用返回封装类
     * */
    @ResponseBody
    @DeleteMapping(value = "/{id}")
    public Result<String> deleteById(@PathVariable Integer id){
        return petService.deleteById(id);
    }

    /**
     * 通过宠物ID查询宠物详细信息
     *
     * @param id 宠物ID
     * @return Result<List<PetVO>>通用返回封装类
     * */
    @ResponseBody
    @GetMapping(value = "/{id}")
    public Result<PetVO> getById(@PathVariable Integer id){
        return petService.getById(id);
    }

    /**
     * 宠物查询信息列表
     *
     * @param petQueryDto
     * @return Result<List<PetListItemVO>>通用返回封装类
     * */
    @Pager
    @ResponseBody
    @PostMapping(value = "/list")
    public Result<List<PetListItemVO>> list(@RequestBody PetQueryDto petQueryDto){
        return petService.list(petQueryDto);
    }


}

