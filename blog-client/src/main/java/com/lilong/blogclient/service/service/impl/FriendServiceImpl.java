package com.lilong.blogclient.service.service.impl;

import com.lilong.blog.enums.FriendStatusEnum;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.vo.friend.SysFriendVo;
import com.lilong.blogclient.service.service.FriendService;
import com.lilong.blogrpc.act.FriendServiceRpc;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendServiceRpc friendServiceRpc;

    @Override
    public List<SysFriendVo> getFriendList() {
        return friendServiceRpc.getFriendList().getData();
    }

    @Override
    public Boolean apply(SysFriendVo sysFriend) {
        SysFriendVo sysFriendVo = friendServiceRpc.selectByUrl(sysFriend.getUrl()).getData();
        if (ObjectUtils.isNotEmpty(sysFriendVo)) {
            throw new ServiceException("申请友链失败，该网站已存在");
        }
        sysFriend.setStatus(FriendStatusEnum.APPLY.getCode());
        friendServiceRpc.save(sysFriend);
        return true;
    }
}
