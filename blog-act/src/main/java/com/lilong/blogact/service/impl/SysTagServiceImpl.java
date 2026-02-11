package com.lilong.blogact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lilong.blog.base.ResultCode;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.vo.tag.TagListVo;
import com.lilong.blogact.entity.SysTag;
import com.lilong.blogact.mapper.SysTagMapper;
import com.lilong.blogact.service.SysTagService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysTagServiceImpl extends ServiceImpl<SysTagMapper, SysTag> implements SysTagService {

    private final SysTagMapper sysTagMapper;

    /**
     * 查询标签表分页列表
     */
    @Override
    public IPage<SysTag> selectPage(SysTag sysTag) {
        LambdaQueryWrapper<SysTag> wrapper = new LambdaQueryWrapper<SysTag>()
                .like(StringUtils.isNotBlank(sysTag.getName()), SysTag::getName, sysTag.getName());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询标签表列表
     */
    @Override
    public List<SysTag> selectList(SysTag sysTag) {
        return list();
    }

    /**
     * 新增标签表
     */
    @Override
    public boolean insert(SysTag sysTag) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysTag>()
                .eq(SysTag::getName, sysTag.getName()));
        if (count > 0) {
            throw new ServiceException(ResultCode.TAG_IS_EXIST.desc);
        }
        return save(sysTag);
    }

    /**
     * 修改标签表
     */
    @Override
    public boolean update(SysTag sysTag) {
        SysTag sysTag1 = baseMapper.selectOne(new LambdaQueryWrapper<SysTag>().eq(SysTag::getName, sysTag.getName()));
        if (sysTag1 != null && !sysTag1.getId().equals(sysTag.getId())) {
            throw new ServiceException(ResultCode.TAG_IS_EXIST.desc);
        }
        return updateById(sysTag);
    }

    /**
     * 批量删除标签表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Integer> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<TagListVo> selectTagList() {
        return sysTagMapper.getTagsApi();
    }
}
