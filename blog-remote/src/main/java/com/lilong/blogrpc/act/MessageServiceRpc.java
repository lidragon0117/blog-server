package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.message.SysMessageVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 18:17
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "messageServiceRpc", configuration = RpcRequestInterceptor.class)
public interface MessageServiceRpc {
    /**
     * 获取留言列表
     *
     * @return
     */
    @GetMapping("/act/message/selectList")
    Result<List<SysMessageVo>> selectList();

    /**
     * 新增留言
     *
     * @param sysMessage
     * @return
     */
    @PostMapping("/act/message/add")
    Result<Void> add(@RequestBody SysMessageVo sysMessage);
}
