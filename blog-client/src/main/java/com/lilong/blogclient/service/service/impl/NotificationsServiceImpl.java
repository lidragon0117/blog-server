package com.lilong.blogclient.service.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.vo.notifications.NotificationsListVo;
import com.lilong.blog.vo.notifications.SysNotificationsVo;
import com.lilong.blogclient.service.service.NotificationsService;
import com.lilong.blogrpc.system.SysNotificationsServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: lilong
 * @date: 2025/3/21
 * @description:
 */
@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {

    private final SysNotificationsServiceRpc sysNotificationsServiceRpc;


    public IPage<NotificationsListVo> page(SysNotificationsVo notifications) {
        notifications.setUserId(CurrentUserHelper.getUserId());
        return sysNotificationsServiceRpc.selectNotificationsPage(PageUtil.getPage(), notifications).getData();
    }

    @Override
    public void doRead(Long id) {
        SysNotificationsVo notifications = sysNotificationsServiceRpc.selectById(id).getData();
        if (notifications == null) {
            throw new ServiceException("消息通知不存在");
        }
        notifications.setIsRead(1);
        sysNotificationsServiceRpc.updateById(notifications);
    }

    @Override
    public void allRead() {
        sysNotificationsServiceRpc.allRead(SysNotificationsVo.builder().isRead(1).build());
    }

    @Override
    public void delete(Long id) {
        sysNotificationsServiceRpc.deleteById(id);
    }

    @Override
    public Map<String, Integer> getUnReadNum() {
        return sysNotificationsServiceRpc.getUnReadNum(CurrentUserHelper.getUserId()).getData();
    }

    @Override
    public Boolean getMyIsUnread() {
        List<SysNotificationsVo> sysNotifications = sysNotificationsServiceRpc.selectListByUserId(CurrentUserHelper.getUserId(), 0).getData();
        return CollectionUtil.isNotEmpty(sysNotifications);
    }

}
