package com.lilong.blogclient.service.service;



import com.lilong.blog.vo.message.SysMessageVo;

import java.util.List;


public interface MessageService {

    /**
     * 获取留言列表
     * @return
     */
    List<SysMessageVo> getMessageList();

    /**
     * 添加留言
     * @param sysMessage
     * @return
     */
    Boolean add(SysMessageVo sysMessage);
}
