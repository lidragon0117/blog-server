package com.lilong.blogclient.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.vo.chat.ChatSendMsgVo;

public interface ChatService {

    /**
     * 获取消息列表
     * @return
     */
    IPage<ChatSendMsgVo> getChatMsgList();

    /**
     * 发送消息
     * @param chatSendMsgVo
     */
    void sendMsg(ChatSendMsgVo chatSendMsgVo);

    /**
     * 撤回消息
     * @param chatSendMsgVo
     */
    void recallMsg(ChatSendMsgVo chatSendMsgVo);
}
