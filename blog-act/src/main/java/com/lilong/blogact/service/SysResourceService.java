package com.lilong.blogact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blogact.entity.SysResource;

import java.util.List;

/**
 * 资源表 服务接口
 */
public interface SysResourceService extends IService<SysResource> {
    /**
     * 查询资源表分页列表
     */
    IPage<SysResource> selectPage(SysResource sysResource);

    /**
     * 新增资源表
     */
    boolean insert(SysResource sysResource);

    /**
     * 修改资源表
     */
    boolean update(SysResource sysResource);

    /**
     * 批量删除资源表
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 查询分页列表
     *
     * @param sysResource
     * @param page
     * @return
     */
    IPage<SysResource> selectPageList(SysResource sysResource, Page page);
}
