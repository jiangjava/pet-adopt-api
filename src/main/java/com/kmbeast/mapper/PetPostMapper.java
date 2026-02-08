package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.PetPostQueryDto;
import com.kmbeast.pojo.entity.PetPost;
import com.kmbeast.pojo.vo.PetPostListItemVO;
import com.kmbeast.pojo.vo.PetPostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宠物经验帖子持久化接口
 */
@Mapper
public interface PetPostMapper extends BaseMapper<PetPost> {

    /**
     * 查询宠物经验帖子信息
     *
     * @param petPostQueryDto 查询条件类
     * @return List<PetPostListItemVO>
     */
    List<PetPostListItemVO> list(PetPostQueryDto petPostQueryDto);

    /**
     * 查询符合条件的记录数 - 配合前端做分页
     *
     * @param petPostQueryDto 查询条件类
     * @return Integer 记录数
     */
    Integer listCount(PetPostQueryDto petPostQueryDto);

    /**
     * 通过主键ID查询宠物帖子信息
     * @param id 主键ID
     * @return PetPostVO
     */
    PetPostVO getById(@Param(value = "id") Integer id);

}