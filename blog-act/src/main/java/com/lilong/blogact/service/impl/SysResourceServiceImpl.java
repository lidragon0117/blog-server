package com.lilong.blogact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blogact.entity.SysResource;
import com.lilong.blogact.mapper.SysResourceMapper;
import com.lilong.blogact.service.SysResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资源表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements SysResourceService {

    /**
     * 查询资源表分页列表
     */
    @Override
    public IPage<SysResource> selectPage(SysResource sysResource) {
        return selectPage(PageUtil.getPage(), sysResource);
    }

    private IPage<SysResource> selectPage(IPage<SysResource> page, SysResource sysResource) {
        LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysResource.getId() != null, SysResource::getId, sysResource.getId());
        wrapper.eq(sysResource.getUserId() != null, SysResource::getUserId, sysResource.getUserId());
        wrapper.eq(sysResource.getName() != null, SysResource::getName, sysResource.getName());
        wrapper.eq(sysResource.getCategory() != null, SysResource::getCategory, sysResource.getCategory());
        wrapper.eq(sysResource.getDownloads() != null, SysResource::getDownloads, sysResource.getDownloads());
        wrapper.eq(sysResource.getIsFree() != null, SysResource::getIsFree, sysResource.getIsFree());
        wrapper.eq(sysResource.getPayType() != null, SysResource::getPayType, sysResource.getPayType());
        wrapper.eq(sysResource.getPanPath() != null, SysResource::getPanPath, sysResource.getPanPath());
        wrapper.eq(sysResource.getPanCode() != null, SysResource::getPanCode, sysResource.getPanCode());
        wrapper.eq(sysResource.getCreateTime() != null, SysResource::getCreateTime, sysResource.getCreateTime());
        return page(page, wrapper);
    }

    /**
     * 新增资源表
     */
    @Override
    public boolean insert(SysResource sysResource) {
        sysResource.setUserId(CurrentUserHelper.getUserId());
        return save(sysResource);
    }

    /**
     * 修改资源表
     */
    @Override
    public boolean update(SysResource sysResource) {
        return updateById(sysResource);
    }

    /**
     * 批量删除资源表
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public IPage<SysResource> selectPageList(SysResource sysResource, Page page) {
        return selectPage(page, sysResource);
    }
}
