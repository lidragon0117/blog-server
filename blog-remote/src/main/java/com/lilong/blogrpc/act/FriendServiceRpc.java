package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.friend.SysFriendVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 17:25
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "friendServiceRpc", configuration = RpcRequestInterceptor.class)
public interface FriendServiceRpc {
    /**
     * 获取友情链接列表
     *
     * @return
     */
    @GetMapping("/act/friend/innerList")
    Result<List<SysFriendVo>> getFriendList();

    /**
     * 申请友链
     *
     * @param sysFriend
     * @return
     */
    @PostMapping("/act/friend/add")
    Result<Void> save(@RequestBody SysFriendVo sysFriend);

    /***
     * 根据url查询友链
     * @param url
     * @return
     */
    @GetMapping("/act/friend/selectByUrl")
    Result<SysFriendVo> selectByUrl(@RequestParam("url") String url);
}
