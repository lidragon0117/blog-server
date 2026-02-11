//package com.lilong.blogclient.service.service.impl;
//
//
//import cn.hutool.core.thread.ThreadUtil;
//import com.alibaba.fastjson.JSON;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.lilong.blog.base.Constants;
//import com.lilong.blog.enums.ChatTypeEnum;
//import com.lilong.blog.utils.BeanCopyUtil;
//import com.lilong.blog.utils.IpUtil;
//import com.lilong.blog.utils.PageUtil;
//import com.lilong.blog.utils.SensitiveUtil;
//import com.lilong.blog.vo.chat.ChatSendMsgVo;
//import com.lilong.blogclient.service.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class ChatServiceImpl implements ChatService {
//
//    private final WebSocketServer webSocketServer;
//
//    private final SysChatMsgMapper chatMsgMapper;
//
//    private final SysUserMapper sysUserMapper;
//
//    private final AiUtil aiUtil;
//
//
//    @Override
//    public IPage<ChatSendMsgVo> getChatMsgList() {
//        return chatMsgMapper.getChatMsgList(PageUtil.getPage());
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void sendMsg(ChatSendMsgVo chatSendMsgVo) {
//
//        //如果是文字类型的，就先敏感词过滤
//        if (ChatTypeEnum.TEXT.getType().equals(chatSendMsgVo.getType())){
//            chatSendMsgVo.setContent(SensitiveUtil.filter(chatSendMsgVo.getContent()));
//        }
//
//        ChatMsg chatMsg = BeanCopyUtil.copyObj(chatSendMsgVo, ChatMsg.class);
//        chatMsg.setSenderId(StpUtil.getLoginIdAsLong());
//        chatMsg.setIp(IpUtil.getIp());
//        chatMsg.setLocation(IpUtil.getIp2region(chatMsg.getIp()));
//
//
//        chatMsgMapper.insert(chatMsg);
//
//        chatSendMsgVo.setId(chatMsg.getId());
//        chatSendMsgVo.setLocation(IpUtil.getIp2region(chatMsg.getIp()));
//        webSocketServer.sendAllMessage(JSON.toJSONString(chatSendMsgVo));
//
//        //判断是否@拾壹小助手
//        String SHINY_XIA_ASSISTANT = "@拾壹小助手";
//        if (chatSendMsgVo.getContent().contains(SHINY_XIA_ASSISTANT)) {
//            ThreadUtil.execAsync(() -> {
//                String replaceContent = chatSendMsgVo.getContent().replace(SHINY_XIA_ASSISTANT, "");
//                if (StringUtils.isBlank(replaceContent)) {
//                    return;
//                }
//                String aiContent = aiUtil.send(replaceContent);
//
//                SysUser sysUser = sysUserMapper.selectById(Constants.XIAO_ASSISTANT_ID);
//                ChatSendMsgVo vo = ChatSendMsgVo.builder()
//                        .avatar(sysUser.getAvatar())
//                        .name(sysUser.getNickname())
//                        .content("@" + chatSendMsgVo.getName() + " " + aiContent)
//                        .userId(Constants.XIAO_ASSISTANT_ID)
//                        .type(ChatTypeEnum.TEXT.getType())
//                        .build();
//                ChatMsg obj = BeanCopyUtil.copyObj(vo, ChatMsg.class);
//                obj.setSenderId(Constants.XIAO_ASSISTANT_ID);
//
//                chatMsgMapper.insert(obj);
//                vo.setId(obj.getId());
//                webSocketServer.sendAllMessage(JSON.toJSONString(vo));
//            });
//        }
//    }
//
//    @Override
//    public void recallMsg(ChatSendMsgVo chatSendMsgVo) {
//        ChatMsg chatMsg1 = chatMsgMapper.selectById(chatSendMsgVo.getId());
//        //判断发送的时间是否超过当前时间俩分钟 LocalDateTime类型
//        if (chatMsg1.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
//            throw new ServiceException("只能撤回俩分钟以内的消息！");
//        }
//
//        if (StpUtil.getLoginIdAsLong() != chatMsg1.getSenderId()) {
//            throw new ServiceException("只能撤回自己的消息！");
//        }
//
//        chatSendMsgVo.setIsRecalled(Boolean.TRUE);
//        chatSendMsgVo.setContent("消息已撤回");
//
//        ChatMsg chatMsg = new ChatMsg();
//        BeanUtils.copyProperties(chatSendMsgVo, chatMsg);
//        chatMsgMapper.updateById(chatMsg);
//
//        webSocketServer.sendAllMessage(JSON.toJSONString(chatSendMsgVo));
//    }
//}
