package com.lilong.blogaigc.service.impl;

/**
 * @author : lilong
 * @date : 2026-02-11 17:03
 * @description :
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blogaigc.entity.SysAiBotPlugin;
import com.lilong.blogaigc.mapper.SysAiBotPluginMapper;
import com.lilong.blogaigc.service.SysAiBotPluginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 插件API管理表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysAiBotPluginServiceImpl extends ServiceImpl<SysAiBotPluginMapper, SysAiBotPlugin> implements SysAiBotPluginService {

    /**
     * 查询插件API管理表分页列表
     */
    @Override
    public IPage<SysAiBotPlugin> selectPage(SysAiBotPlugin sysAiBotPlugin) {
        LambdaQueryWrapper<SysAiBotPlugin> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysAiBotPlugin.getId() != null, SysAiBotPlugin::getId, sysAiBotPlugin.getId());
        wrapper.eq(sysAiBotPlugin.getBotCode() != null, SysAiBotPlugin::getBotCode, sysAiBotPlugin.getBotCode());
        wrapper.eq(sysAiBotPlugin.getApi() != null, SysAiBotPlugin::getApi, sysAiBotPlugin.getApi());
        wrapper.eq(sysAiBotPlugin.getMethod() != null, SysAiBotPlugin::getMethod, sysAiBotPlugin.getMethod());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询插件API管理表列表
     */
    @Override
    public List<SysAiBotPlugin> selectList(SysAiBotPlugin sysAiBotPlugin) {
        LambdaQueryWrapper<SysAiBotPlugin> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysAiBotPlugin.getId() != null, SysAiBotPlugin::getId, sysAiBotPlugin.getId());
        wrapper.eq(sysAiBotPlugin.getBotCode() != null, SysAiBotPlugin::getBotCode, sysAiBotPlugin.getBotCode());
        wrapper.eq(sysAiBotPlugin.getApi() != null, SysAiBotPlugin::getApi, sysAiBotPlugin.getApi());
        wrapper.eq(sysAiBotPlugin.getMethod() != null, SysAiBotPlugin::getMethod, sysAiBotPlugin.getMethod());
        return list(wrapper);
    }

    /**
     * 新增插件API管理表
     */
    @Override
    public boolean insert(SysAiBotPlugin sysAiBotPlugin) {
        return save(sysAiBotPlugin);
    }

    /**
     * 修改插件API管理表
     */
    @Override
    public boolean update(SysAiBotPlugin sysAiBotPlugin) {
        return updateById(sysAiBotPlugin);
    }

    /**
     * 批量删除插件API管理表
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}