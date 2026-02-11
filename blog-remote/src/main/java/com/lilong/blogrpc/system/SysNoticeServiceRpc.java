package com.lilong.blogrpc.system;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.notice.SysNoticeVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 18:05
 * @description :
 */
@FeignClient(name = "blog-system-service", contextId = "sysNoticeServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysNoticeServiceRpc {
    /**
     * 获取公告列表
     *
     * @return
     */
    @GetMapping("/sys/notice/selectList")
    Result<List<SysNoticeVo>> selectNoticeList(@SpringQueryMap SysNoticeVo sysNoticeVo);

}
