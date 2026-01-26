package com.kmbeast.service.impl;

import com.kmbeast.mapper.PetMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.PetQueryDto;
import com.kmbeast.pojo.em.IsAdoptEnum;
import com.kmbeast.pojo.em.IsRecommendEnum;
import com.kmbeast.pojo.entity.Pet;
import com.kmbeast.pojo.vo.PetListItemVO;
import com.kmbeast.pojo.vo.PetVO;
import com.kmbeast.service.PetService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物信息业务逻辑实现类
 */
@Service
public class PetServiceImpl implements PetService {

    @Resource
    private PetMapper petMapper;

    /**
     * 宠物信息新增
     *
     * @param pet 实体信息
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> save(Pet pet) {
        petParamCheck(pet);
        pet.setIsAdopt(IsAdoptEnum.No_ADOPT.getStats()); // 初始状态未领养
        pet.setCreateTime(LocalDateTime.now());
        petMapper.save(pet);
        return ApiResult.success("宠物信息新增成功");
    }

    /**
     * 宠物信息校验
     * @param pet 宠物信息
     */
    private void petParamCheck(Pet pet) {
        AssertUtils.hasText(pet.getName(), "宠物名不能为空");
        AssertUtils.hasText(pet.getCover(), "请上传一张封面");
        AssertUtils.hasText(pet.getAddress(), "请补充宠物所在地");
        AssertUtils.hasText(pet.getDetail(), "请补充宠物描述");
        AssertUtils.notNull(pet.getPetTypeId(), "请选择宠物类型");
        AssertUtils.notNull(pet.getAge(), "请填写宠物年龄");
        AssertUtils.notNull(pet.getIsVaccine(), "请选择宠物是否已经接种疫苗");
        AssertUtils.notNull(pet.getIsRecommend(), "请选择是否推荐");
    }

    /**
     * 宠物信息修改
     *
     * @param pet 实体信息
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> update(Pet pet) {
        petParamCheck(pet);
        petMapper.update(pet);
        return ApiResult.success("宠物信息修改成功");
    }

    /**
     * 通过ID删除宠物
     *
     * @param id 主键ID
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<String> deleteById(Integer id) {
        petMapper.deleteById(id);
        return ApiResult.success("宠物信息删除成功");
    }

    /**
     * 通过宠物ID查询宠物信息
     *
     * @param id 主键ID
     * @return Result<String> 后台通用返回封装类
     */
    @Override
    public Result<PetVO> getById(Integer id) {
        PetVO petVO = petMapper.getById(id);
        return ApiResult.success(petVO);
    }

    /**
     * 查询宠物列表
     *
     * @param petQueryDto 查询条件类
     * @return Result<List < PetListItemVO>>  后台通用返回封装类
     */
    @Override
    public Result<List<PetListItemVO>> list(PetQueryDto petQueryDto) {
        List<PetListItemVO> petListItemVOS = petMapper.queryListItem(petQueryDto);
        Integer count = petMapper.queryCount(petQueryDto);
        return ApiResult.success(petListItemVOS, count);
    }

    /**
     * 查询手动推荐的宠物数据，类似于banner效果
     * @return Result<List < PetListItemVO>>
     */
    @Override
    public Result<List<PetListItemVO>> recommend() {
        PetQueryDto petQueryDto = new PetQueryDto();
        petQueryDto.setIsRecommend(IsRecommendEnum.RECOMMEND.getStats());
        List<PetListItemVO> petListItemVOS = petMapper.queryListItem(petQueryDto);
        if(!petListItemVOS.isEmpty()){
            return ApiResult.success(petListItemVOS);
        }
        // 如果系统没有推荐
        PetQueryDto defaultPetQueryDto = new PetQueryDto();
        defaultPetQueryDto.setCurrent(0);
        defaultPetQueryDto.setSize(3);
        List<PetListItemVO> defaultPetListItemVOS = petMapper.queryListItem(defaultPetQueryDto);
        return ApiResult.success(defaultPetListItemVOS);
    }
}