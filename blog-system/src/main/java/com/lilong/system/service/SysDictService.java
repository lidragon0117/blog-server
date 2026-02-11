package com.lilong.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.system.entity.SysDict;

public interface SysDictService extends IService<SysDict> {
    /**
     * 分页查询字典
     */
    IPage<SysDict> getDictPageList(String name,Integer status);
    
    /**
     * 新增字典
     */
    void addDict(SysDict dict);
    
    /**
     * 更新字典
     */
    void updateDict(SysDict dict);
    
    /**
     * 删除字典
     */
    void deleteDict(Long id);
} 