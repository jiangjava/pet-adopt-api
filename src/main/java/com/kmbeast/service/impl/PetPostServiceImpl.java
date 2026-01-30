package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.PetPostMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.PetPostQueryDto;
import com.kmbeast.pojo.dto.PetTypeQueryDto;
import com.kmbeast.pojo.em.IsAuditEnum;
import com.kmbeast.pojo.entity.PetPost;
import com.kmbeast.pojo.vo.PetPostListItemVO;
import com.kmbeast.pojo.vo.PetPostVO;
import com.kmbeast.service.PetPostService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物经验帖子业务逻辑接口实现类
 */
@Service
public class PetPostServiceImpl extends ServiceImpl<PetPostMapper, PetPost> implements PetPostService {

    /**
     * 经验帖子列表查询
     * @param petTypeQueryDto 查询条件
     * @return List<PetPostListItemVO> 后台通用响应
     */
    @Override
    public Result<List<PetPostListItemVO>> list(PetTypeQueryDto petTypeQueryDto) {
        List<PetPostListItemVO> petPostListItemVOS = this.baseMapper.list(petTypeQueryDto);
        Integer count = this.baseMapper.listCount(petTypeQueryDto);
        return ApiResult.success(petPostListItemVOS, count);
    }

    /**
     * 通过ID查询咨询宠物帖子信息
     * @param id 宠物帖子主键ID
     * @return Result<PetPostVO>
     */
    @Override
    public Result<PetPostVO> getById(Integer id) {
        PetPostVO petPostVO = this.baseMapper.getById(id);
        return ApiResult.success(petPostVO);
    }

    /**
     * 用户经验新增
     * @param petPost 实体数据
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(PetPost petPost) {
        // 先做参数校验
        judgeParam(petPost);
        petPost.setUserId(LocalThreadHolder.getUserId());//发布者用户ID
        petPost.setCreateTime(LocalDateTime.now());// 设置发布时间
        petPost.setIsAudit(IsAuditEnum.No_AUDIT.getStats());// 发布时,初始是未审核的
        save(petPost);
        return ApiResult.success("宠物经验帖子新增成功");
    }

    private void judgeParam(PetPost petPost) {
        AssertUtils.notNull(petPost.getPetTypeId(),"宠物类型不为空哦");
        AssertUtils.hasText(petPost.getTitle(),"标题不为空哦");
        AssertUtils.hasText(petPost.getCover(),"封面要上传哦");
        AssertUtils.hasText(petPost.getSummary(),"请补充摘要");
        updateById(petPost);
    }

    /**
     * 宠物经验帖子修改
     * @param petPost 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> updateEntity(PetPost petPost) {
        // 先做参数校验
        judgeParam(petPost);
        return ApiResult.success("宠物经验帖子修改成功");
    }

    @Override
    public Result<List<PetPostListItemVO>> list(PetPostQueryDto petPostQueryDto) {
        return null;
    }
}
