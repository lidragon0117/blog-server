package com.lilong.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.system.entity.SysDictData;
import com.lilong.system.entity.SysNotifications;

import java.util.List;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-10 19:44
 * @description :
 */
public interface SysNotificationsService extends IService<SysNotifications> {
    /**
     *  根据用户ID查询消息通知对象
     * @param userId
     * @param isRead
     * @return
     */
    List<SysNotifications> selectListByUserId(Long userId, int isRead);

    /**
     * 查询用户未读数量
     * @param userId
     * @return
     */
    Map<String, Integer> getUnReadNum(Long userId);

    /**
     * 全部已读
     * @param sysNotifications
     */
    void updateAllRead(SysNotifications sysNotifications);
}
