package com.lilong.blogact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.vo.resource.SysResourceVo;
import com.lilong.blogact.entity.SysResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资源表 Mapper接口
 */
@Mapper
public interface SysResourceMapper extends BaseMapper<SysResource> {

    Page<SysResourceVo> getResourceList(@Param("page") Page<Object> page, @Param("sysResource") SysResource sysResource);

}
