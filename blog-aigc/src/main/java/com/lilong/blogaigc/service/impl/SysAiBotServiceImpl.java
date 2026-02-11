package com.lilong.blogaigc.service.impl;

/**
 * @author : lilong
 * @date : 2026-02-11 16:55
 * @description :
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blogaigc.entity.SysAiBot;
import com.lilong.blogaigc.mapper.SysAiBotMapper;
import com.lilong.blogaigc.service.SysAiBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 插件管理表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysAiBotServiceImpl extends ServiceImpl<SysAiBotMapper, SysAiBot> implements SysAiBotService {

    /**
     * 查询插件管理表分页列表
     */
    @Override
    public IPage<SysAiBot> selectPage(SysAiBot sysAiBot) {
        LambdaQueryWrapper<SysAiBot> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysAiBot.getId() != null, SysAiBot::getId, sysAiBot.getId());
        wrapper.eq(sysAiBot.getName() != null, SysAiBot::getName, sysAiBot.getName());
        wrapper.eq(sysAiBot.getCode() != null, SysAiBot::getCode, sysAiBot.getCode());
        wrapper.eq(sysAiBot.getDescription() != null, SysAiBot::getDescription, sysAiBot.getDescription());
        wrapper.eq(sysAiBot.getStatus() != null, SysAiBot::getStatus, sysAiBot.getStatus());
        wrapper.eq(sysAiBot.getSchema() != null, SysAiBot::getSchema, sysAiBot.getSchema());
        wrapper.eq(sysAiBot.getRule() != null, SysAiBot::getRule, sysAiBot.getRule());
        wrapper.eq(sysAiBot.getOutputExample() != null, SysAiBot::getOutputExample, sysAiBot.getOutputExample());
        wrapper.eq(sysAiBot.getCreateBy() != null, SysAiBot::getCreateBy, sysAiBot.getCreateBy());
        wrapper.eq(sysAiBot.getCreateDate() != null, SysAiBot::getCreateDate, sysAiBot.getCreateDate());
        wrapper.eq(sysAiBot.getUpdateBy() != null, SysAiBot::getUpdateBy, sysAiBot.getUpdateBy());
        wrapper.eq(sysAiBot.getUpdateDate() != null, SysAiBot::getUpdateDate, sysAiBot.getUpdateDate());
        wrapper.eq(sysAiBot.getVersion() != null, SysAiBot::getVersion, sysAiBot.getVersion());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询插件管理表列表
     */
    @Override
    public List<SysAiBot> selectList(SysAiBot sysAiBot) {
        LambdaQueryWrapper<SysAiBot> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysAiBot.getId() != null, SysAiBot::getId, sysAiBot.getId());
        wrapper.eq(sysAiBot.getName() != null, SysAiBot::getName, sysAiBot.getName());
        wrapper.eq(sysAiBot.getCode() != null, SysAiBot::getCode, sysAiBot.getCode());
        wrapper.eq(sysAiBot.getDescription() != null, SysAiBot::getDescription, sysAiBot.getDescription());
        wrapper.eq(sysAiBot.getStatus() != null, SysAiBot::getStatus, sysAiBot.getStatus());
        wrapper.eq(sysAiBot.getSchema() != null, SysAiBot::getSchema, sysAiBot.getSchema());
        wrapper.eq(sysAiBot.getRule() != null, SysAiBot::getRule, sysAiBot.getRule());
        wrapper.eq(sysAiBot.getOutputExample() != null, SysAiBot::getOutputExample, sysAiBot.getOutputExample());
        wrapper.eq(sysAiBot.getCreateBy() != null, SysAiBot::getCreateBy, sysAiBot.getCreateBy());
        wrapper.eq(sysAiBot.getCreateDate() != null, SysAiBot::getCreateDate, sysAiBot.getCreateDate());
        wrapper.eq(sysAiBot.getUpdateBy() != null, SysAiBot::getUpdateBy, sysAiBot.getUpdateBy());
        wrapper.eq(sysAiBot.getUpdateDate() != null, SysAiBot::getUpdateDate, sysAiBot.getUpdateDate());
        wrapper.eq(sysAiBot.getVersion() != null, SysAiBot::getVersion, sysAiBot.getVersion());
        return list(wrapper);
    }

    /**
     * 新增插件管理表
     */
    @Override
    public boolean insert(SysAiBot sysAiBot) {
        return save(sysAiBot);
    }

    /**
     * 修改插件管理表
     */
    @Override
    public boolean update(SysAiBot sysAiBot) {
        return updateById(sysAiBot);
    }

    /**
     * 批量删除插件管理表
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}