package com.lilong.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.system.entity.SysNotifications;
import com.lilong.system.mapper.SysNotificationsMapper;
import com.lilong.system.service.SysNotificationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-10 19:45
 * @description :
 */
@Slf4j
@Service
public class SysNotificationsServiceImpl extends ServiceImpl<SysNotificationsMapper, SysNotifications> implements SysNotificationsService {

    @Autowired
    private SysNotificationsMapper sysNotificationsMapper;

    @Override
    public List<SysNotifications> selectListByUserId(Long userId, int isRead) {
        List<SysNotifications> sysNotifications = baseMapper.selectList(new LambdaQueryWrapper<SysNotifications>()
                .eq(SysNotifications::getUserId, userId)
                .eq(SysNotifications::getIsRead, isRead));
        return sysNotifications;
    }

    @Override
    public Map<String, Integer> getUnReadNum(Long userId) {
        return sysNotificationsMapper.getUnReadNum(userId);
    }

    @Override
    public void updateAllRead(SysNotifications sysNotifications) {
        baseMapper.update(sysNotifications, new LambdaQueryWrapper<SysNotifications>()
                .eq(SysNotifications::getUserId, CurrentUserHelper.getUserId()));
    }
}
