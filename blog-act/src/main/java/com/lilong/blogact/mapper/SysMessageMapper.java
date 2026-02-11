package com.lilong.blogact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilong.blogact.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言 Mapper接口
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}