package com.lilong.blogaigc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blogaigc.entity.SysAiBotPlugin;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 17:03
 * @description :
 */
public interface SysAiBotPluginService extends IService<SysAiBotPlugin> {
    /**
     * 查询插件API管理表分页列表
     */
    IPage<SysAiBotPlugin> selectPage(SysAiBotPlugin sysAiBotPlugin);

    /**
     * 查询插件API管理表列表
     */
    List<SysAiBotPlugin> selectList(SysAiBotPlugin sysAiBotPlugin);

    /**
     * 新增插件API管理表
     */
    boolean insert(SysAiBotPlugin sysAiBotPlugin);

    /**
     * 修改插件API管理表
     */
    boolean update(SysAiBotPlugin sysAiBotPlugin);

    /**
     * 批量删除插件API管理表
     */
    boolean deleteByIds(List<Long> ids);
}