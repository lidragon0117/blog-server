package com.lilong.blogaigc.mapper;

/**
 * @author : lilong
 * @date : 2026-02-11 17:02
 * @description :
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilong.blogaigc.entity.SysAiBotPlugin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 插件API管理表 Mapper接口
 */
@Mapper
public interface SysAiBotPluginMapper extends BaseMapper<SysAiBotPlugin> {
}