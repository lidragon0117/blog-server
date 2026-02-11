package com.lilong.blogclient.service.service;



import com.lilong.blog.vo.friend.SysFriendVo;

import java.util.List;

public interface FriendService {

    /**
     * 获取友情链接列表
     * @return
     */
    List<SysFriendVo> getFriendList();

    /**
     * 申请友链
     * @param sysFriend
     * @return
     */
    Boolean apply(SysFriendVo sysFriend);
}
