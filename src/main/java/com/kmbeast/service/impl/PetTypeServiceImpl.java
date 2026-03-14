package com.kmbeast.service.impl;

import com.kmbeast.config.RabbitCacheConfig;
import com.kmbeast.mapper.PetTypeMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.PetTypeQueryDto;
import com.kmbeast.pojo.entity.PetType;
import com.kmbeast.service.PetTypeService;
import com.kmbeast.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* 宠物类别业务逻辑实现类
* */
@Slf4j
@Service
public class PetTypeServiceImpl implements PetTypeService {

    @Resource
    private PetTypeMapper petTypeMapper;
    @Resource
    private PetTypeService petTypeService;
    @Autowired
    private RabbitTemplate rabbitTemplate;  // 注入 RabbitTemplate

    /**
    * 宠物类别新增
    * @param petType 实体数据
    * @return Result<String>后台通用返回封装类
    * */
    @Override
    @CacheEvict(value = "petType", allEntries = true)
    public Result<String> save(PetType petType) {
        //确保传进来的宠物名不能为空
        AssertUtils.hasText(petType.getName(),"宠物类别名不能为空");
        //如果传进来的宠物类别名如果系统已经存在则不能在进行新增
        PetType petTypeEntity = petTypeMapper.queryByName(petType.getName());
        AssertUtils.isTrue(petTypeEntity == null,"宠物类别名已经存在");
        petTypeMapper.save(petType);
        return ApiResult.success("宠物类别新增成功");
    }


    /**
     * 宠物类别修改
     * @param petType 实体数据
     * @return Result<String>后台通用返回封装类
     * */
    @Override
    @CacheEvict(value = "petType", allEntries = true)
    public Result<String> update(PetType petType) {
        //确保传进来的宠物名不能为空
        AssertUtils.hasText(petType.getName(),"宠物类别名不能为空");
        //如果传进来的宠物类别名如果系统已经存在则不能在进行新增
        PetType petTypeEntity = petTypeMapper.queryByName(petType.getName());
        AssertUtils.isTrue(petTypeEntity == null,"宠物类别名已经存在");
        petTypeMapper.update(petType);

        // 更新数据库
        petTypeMapper.update(petType);
        //    发送缓存删除消息（异步）
        //    由于 @CacheEvict 清除了整个区域，我们可以发送一个代表“清空类别缓存”的消息
        //    例如约定一个特殊的 key，消费者收到后执行对应的清理逻辑
        String cacheRegion = "petType";  // 或者用具体的 key，但 allEntries 需要清空多个 key
        rabbitTemplate.convertAndSend(
                RabbitCacheConfig.CACHE_EXCHANGE,
                RabbitCacheConfig.CACHE_ROUTING_KEY,
                cacheRegion  // 发送区域标识，消费者根据此执行清空操作
        );
        log.info("已发送类别缓存清空消息: {}", cacheRegion);
        return ApiResult.success("宠物类别修改成功");
    }

    /**
     * 宠物类别删除
     * @param id 主键ID
     * @return Result<String>后台通用返回封装类
     * */
    @Override
    @CacheEvict(value = "petType", allEntries = true)
    public Result<String> deleteById(Integer id) {
        petTypeMapper.deleteById(id);
        return ApiResult.success("宠物类别删除成功");
    }

    /**
     * 宠物类别查询
     * @param petTypeQueryDto 查询条件类
     * @return Result<String>后台通用返回封装类
     * */
    @Override
    public Result<List<PetType>> query(PetTypeQueryDto petTypeQueryDto) {
        //缓存预热
        petTypeService.getAllTypes();
        //查询符合条件的总条数 - 前端分页用的
        Integer count = petTypeMapper.queryCount(petTypeQueryDto);
        //查符合条件的数据项
        List<PetType> petTypeList = petTypeMapper.query(petTypeQueryDto);
        return ApiResult.success(petTypeList, count);
    }

    @Override
    @Cacheable(value = "petType", key = "'all'")
    public List<PetType> getAllTypes() {
        PetTypeQueryDto dto = new PetTypeQueryDto();
        return petTypeMapper.query(dto);
    }
}
