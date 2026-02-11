package com.lilong.blogact.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilong.blogact.entity.SysPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/2/7
 * @description:
 */
@Mapper
public interface SysPhotoMapper extends BaseMapper<SysPhoto> {

    void move(@Param("ids") List<Long> ids, @Param("albumId") Long albumId);
}
