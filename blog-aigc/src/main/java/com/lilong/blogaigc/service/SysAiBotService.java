package com.lilong.blogaigc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blogaigc.entity.SysAiBot;

import java.util.List;

/**
 * 插件管理表 服务接口
 */
public interface SysAiBotService extends IService<SysAiBot> {
    /**
     * 查询插件管理表分页列表
     */
    IPage<SysAiBot> selectPage(SysAiBot sysAiBot);

    /**
     * 查询插件管理表列表
     */
    List<SysAiBot> selectList(SysAiBot sysAiBot);

    /**
     * 新增插件管理表
     */
    boolean insert(SysAiBot sysAiBot);

    /**
     * 修改插件管理表
     */
    boolean update(SysAiBot sysAiBot);

    /**
     * 批量删除插件管理表
     */
    boolean deleteByIds(List<Long> ids);
}