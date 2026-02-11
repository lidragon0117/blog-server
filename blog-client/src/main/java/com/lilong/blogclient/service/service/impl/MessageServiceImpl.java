package com.lilong.blogclient.service.service.impl;

import com.lilong.blog.utils.IpUtil;
import com.lilong.blog.utils.SensitiveUtil;
import com.lilong.blog.vo.message.SysMessageVo;

import com.lilong.blogclient.service.service.MessageService;
import com.lilong.blogrpc.act.MessageServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageServiceRpc messageServiceRpc;

    @Override
    public List<SysMessageVo> getMessageList() {
        return messageServiceRpc.selectList().getData();
    }

    @Override
    public Boolean add(SysMessageVo sysMessage) {
        String ip = IpUtil.getIp();
        sysMessage.setIp(ip);
        sysMessage.setSource(IpUtil.getIp2region(ip));
        sysMessage.setContent(SensitiveUtil.filter(sysMessage.getContent()));
        messageServiceRpc.add(sysMessage);
        return true;
    }
}
