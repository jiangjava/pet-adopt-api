package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.NoticeMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.NoticeQueryDto;
import com.kmbeast.pojo.entity.Notice;
import com.kmbeast.pojo.vo.NoticeListItemVO;
import com.kmbeast.service.NoticeService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
* 公告业务逻辑实现类
* */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    /**
     * 公告新增
     * @param notice 实体
     * @return Result<String>
     */
    @Override
    @CacheEvict(value = "notice", allEntries = true)
    public Result<String> saveEntity(Notice notice) {
        AssertUtils.hasText(notice.getTitle(),"公告内容不能为空");
        notice.setCreateTime(LocalDateTime.now());
        save(notice);
        return ApiResult.success("公告新增成功");
    }

    /**
     * 公告修改
     * @param notice 实体
     * @return Result<String>
     */
    @Override
    @CacheEvict(value = "notice", allEntries = true)
    public Result<String> update(Notice notice) {
        AssertUtils.hasText(notice.getTitle(),"公告标题不能为空");
        notice.setCreateTime(LocalDateTime.now());
        updateById(notice);
        return ApiResult.success("公告修改成功");
    }

    /**
     * 公告删除（同时清空公告缓存）
     */
    @Override
    @CacheEvict(value = "notice", allEntries = true)
    public Result<String> deleteById(Integer id) {
        removeById(id);
        return ApiResult.success("公告删除成功");
    }

    /**
     * 公告查询（按入参缓存，相同条件命中缓存）
     * @param noticeQueryDto 查询参数条件
     * @return Result<List<NoticeListItemVO>>
     */
    @Override
    @Cacheable(value = "notice", key = "(#noticeQueryDto.current != null ? #noticeQueryDto.current : 1) + '_' + (#noticeQueryDto.size != null ? #noticeQueryDto.size : 10) + '_' + (#noticeQueryDto.title != null ? #noticeQueryDto.title : '')")
    public Result<List<NoticeListItemVO>> query(NoticeQueryDto noticeQueryDto) {
        List<NoticeListItemVO> noticeList = noticeMapper.list(noticeQueryDto);
        Integer count = noticeMapper.listCount(noticeQueryDto);
        return ApiResult.success(noticeList, count);
    }

}
