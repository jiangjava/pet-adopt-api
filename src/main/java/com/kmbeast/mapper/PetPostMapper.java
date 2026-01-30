package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.PetTypeQueryDto;
import com.kmbeast.pojo.entity.PetPost;
import com.kmbeast.pojo.vo.PetPostListItemVO;
import com.kmbeast.pojo.vo.PetPostVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *宠物经验帖子持久化接口
 */

public interface PetPostMapper extends BaseMapper<PetPost> {

    /**
     * 查询宠物经验帖子信息
     *
     * @param petTypeQueryDto 查询条件类
     * @return List<ActiveNet>
     */
    List<PetPostListItemVO> list(PetTypeQueryDto petTypeQueryDto);

    /**
     * 查询符合条件的记录数 - 配合前端做分页
     * @param petTypeQueryDto 查询条件类
     * @return Integer 记录数
     */
    Integer listCount(PetTypeQueryDto petTypeQueryDto);

    /**
     * 通过Id查询宠物帖子信息
     * @param id 主键id
     * @return PetPostVO
     */
    PetPostVO getById(@Param(value = "id") Integer id);
}
