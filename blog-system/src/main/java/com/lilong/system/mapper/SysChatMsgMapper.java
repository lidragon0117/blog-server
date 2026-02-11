package com.lilong.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.vo.chat.ChatSendMsgVo;
import com.lilong.system.entity.ChatMsg;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysChatMsgMapper extends BaseMapper<ChatMsg> {

    IPage<ChatSendMsgVo> getChatMsgList(Page<Object> page);
}
