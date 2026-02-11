package com.lilong.blogclient.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.vo.resource.SysResourceVo;


/**
 * @author: lilong
 * @date: 2025/3/12
 * @description:
 */
public interface ResourceService {

    Page<SysResourceVo> getResourceList(SysResourceVo sysResource);

    void add(SysResourceVo sysResource);

    SysResourceVo verify(String code,Long id);
}
