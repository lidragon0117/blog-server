package com.lilong.blogrpc.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.notifications.NotificationsListVo;
import com.lilong.blog.vo.notifications.SysNotificationsVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-10 19:25
 * @description :
 */
@FeignClient(name = "blog-system-service", contextId = "sysNotificationsServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysNotificationsServiceRpc {
    /**
     * 根据用户Id查询消息通知
     *
     * @param userId
     * @param isRead
     * @return
     */
    @GetMapping("/sys/notifications/selectListByUserId")
    Result<List<SysNotificationsVo>> selectListByUserId(@RequestParam("userId") Long userId, @RequestParam("isRead") int isRead);

    /**
     * 根据ID删除数据
     *
     * @param id
     * @return
     */
    @GetMapping("/sys/notifications/deleteById")
    Result<Void> deleteById(@RequestParam("id") Long id);

    /**
     * 获取未读数量
     *
     * @param userId
     * @return
     */
    @GetMapping("/sys/notifications/getUnReadNum")
    Result<Map<String, Integer>> getUnReadNum(@RequestParam("userId") Long userId);

    /**
     * 获取消息通知详情
     *
     * @param id
     * @return
     */
    @GetMapping("sys/notifications/detail")
    Result<SysNotificationsVo> selectById(@RequestParam("id") Long id);

    /**
     * 根据Id进行更新
     *
     * @param notifications
     * @return
     */
    @PostMapping("sys/notifications/updateById")
    Result<Void> updateById(@RequestBody SysNotificationsVo notifications);

    /**
     * 全部已读
     *
     * @param build
     * @return
     */
    @PostMapping("sys/notifications/allRead")
    Result<Void> allRead(@RequestBody SysNotificationsVo build);

    /**
     * 分页查询
     *
     * @param page
     * @param notifications
     * @return
     */
    @PostMapping("/sys/notifications/page")
    Result<IPage<NotificationsListVo>> selectNotificationsPage(@RequestParam("page") Page<Object> page, @RequestBody SysNotificationsVo notifications);
}
