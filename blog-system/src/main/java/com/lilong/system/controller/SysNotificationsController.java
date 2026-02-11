package com.lilong.system.controller;

import com.lilong.blog.base.Result;
import com.lilong.system.entity.SysNotifications;
import com.lilong.system.service.SysNotificationsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-10 19:47
 * @description :
 */
@RestController
@RequestMapping("/sys/notifications")
public class SysNotificationsController {
    @Autowired
    private SysNotificationsService sysNotificationsService;

    @ApiOperation(value = "根据用户查询消息通知")
    @GetMapping("/selectListByUserId")
    Result<List<SysNotifications>> selectListByUserId(@RequestParam("userId") Long userId, @RequestParam("isRead") int isRead) {
        return Result.success(sysNotificationsService.selectListByUserId(userId, isRead));
    }

    @GetMapping("/deleteById")
    @ApiOperation(value = "根据id删除消息通知")
    Result<Void> deleteById(@RequestParam("id") Long id) {
        sysNotificationsService.removeById(id);
        return Result.success();
    }

    @GetMapping("/getUnReadNum")
    @ApiOperation(value = "获取未读数量")
    Result<Map<String, Integer>> getUnReadNum(@RequestParam("userId") Long userId) {
        return Result.success(sysNotificationsService.getUnReadNum(userId));
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取详细信息")
    Result<SysNotifications> selectById(@RequestParam("id") Long id) {
        return Result.success(sysNotificationsService.getById(id));
    }

    @PostMapping("/updateById")
    @ApiOperation(value = "根据ID进行更新")
    Result<Void> updateById(@RequestBody SysNotifications notifications) {
        sysNotificationsService.updateById(notifications);
        return Result.success();
    }


    @PostMapping("sys/notifications/allRead")
    @ApiOperation(value = "全部已读")
    Result<Void> allRead(@RequestBody SysNotifications sysNotifications) {
        sysNotificationsService.updateAllRead(sysNotifications);
        return Result.success();
    }

}
