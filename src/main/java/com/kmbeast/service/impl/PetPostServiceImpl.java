package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.ActiveNetMapper;
import com.kmbeast.mapper.PetPostMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.ActiveNetQueryDto;
import com.kmbeast.pojo.dto.PetPostQueryDto;
import com.kmbeast.pojo.em.ActiveNetType;
import com.kmbeast.pojo.em.IsAuditEnum;
import com.kmbeast.pojo.entity.ActiveNet;
import com.kmbeast.pojo.entity.PetPost;
import com.kmbeast.pojo.vo.PetPostListItemVO;
import com.kmbeast.pojo.vo.PetPostVO;
import com.kmbeast.service.PetPostService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物经验帖子业务逻辑接口实现类
 */
@Service
public class PetPostServiceImpl extends ServiceImpl<PetPostMapper, PetPost> implements PetPostService {

    @Resource
    private ActiveNetMapper activeNetMapper;

    /**
     * 经验帖子列表查询
     *
     * @param petPostQueryDto 查询条件
     * @return Result<List < PetPostListItemVO>> 后台通用响应
     */
    @Override
    public Result<List<PetPostListItemVO>> list(PetPostQueryDto petPostQueryDto) {
        List<PetPostListItemVO> petPostListItemVOS = this.baseMapper.list(petPostQueryDto);
        Integer count = this.baseMapper.listCount(petPostQueryDto);
        return ApiResult.success(petPostListItemVOS, count);
    }

    /**
     * 通过ID查询宠物帖子信息
     *
     * @param id 宠物帖子主键ID
     * @return Result<PetPostVO>
     */
    @Override
    public Result<PetPostVO> getById(Integer id) {
        PetPostVO petPostVO = this.baseMapper.getById(id);
        //浏览逻辑实现
        ActiveNetQueryDto activeNetQueryDto = new ActiveNetQueryDto();
        activeNetQueryDto.setId(LocalThreadHolder.getUserId());//设置上用户ID
        activeNetQueryDto.setContentId(id); //设置内容ID
        activeNetQueryDto.setContentType("PET-POST"); //标识查的是宠物类型模块
        activeNetQueryDto.setType(ActiveNetType.VIEW.getStatus()); // 声明为浏览类型
        Integer count = activeNetMapper.queryCount(activeNetQueryDto);
        if (count == 0) { //证明用户没有针对宠物模块下面的宠物信息浏览过
            ActiveNet activeNet = new ActiveNet();
            activeNet.setUserId(LocalThreadHolder.getUserId());
            activeNet.setUserId(id);
            activeNet.setContentType("PET-POST");
            activeNet.setCreateTime(LocalDateTime.now());
            activeNetMapper.insert(activeNet); //浏览记录新增
        }
        return ApiResult.success(petPostVO);
    }

    /**
     * 经验帖子新增
     *
     * @param petPost 实体数据
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(PetPost petPost) {
        judgeParam(petPost); // 先做参数校验
        petPost.setUserId(LocalThreadHolder.getUserId()); // 设置发布者用户ID
        petPost.setCreateTime(LocalDateTime.now()); // 设置发布时间
        petPost.setIsAudit(IsAuditEnum.NO_AUDIT.getStats()); // 发布时，初始是未审核的
        save(petPost);
        return ApiResult.success("宠物经验帖子新增成功");
    }

    private void judgeParam(PetPost petPost) {
        AssertUtils.notNull(petPost.getPetTypeId(), "宠物类型不为空哦");
        AssertUtils.hasText(petPost.getTitle(), "标题不为空哦");
        AssertUtils.hasText(petPost.getCover(), "封面要上传哦");
        AssertUtils.hasText(petPost.getSummary(), "请补充摘要");
        AssertUtils.isTrue(petPost.getTitle().length() < 30, "标题长度最多30个字哦");
        AssertUtils.isTrue(petPost.getSummary().length() < 200, "摘要长度要控制在200个字以内哦");
    }

    /**
     * 宠物经验帖子修改
     *
     * @param petPost 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> updateEntity(PetPost petPost) {
        judgeParam(petPost); // 先做参数校验
        updateById(petPost);
        return ApiResult.success("宠物经验帖子修改成功");
    }

    /**
     * 宠物经验帖子审核
     *
     * @param id 主键ID
     * @return Result<String> 通用返回封装类
     */
    @Override
    public Result<String> audit(Integer id) {
        PetPost petPost = new PetPost();
        petPost.setId(id);
        petPost.setIsAudit(IsAuditEnum.AUDIT.getStats());
        updateById(petPost);
        return ApiResult.success("审核成功");
    }
}