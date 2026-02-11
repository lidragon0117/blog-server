package com.lilong.blogact.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blogact.entity.SysMessage;


/**
 * @author: lilong
 * @date: 2025/1/2
 * @description:
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 获取消息列表
     *
     * @return
     */
    Page<SysMessage> selectList();


}
