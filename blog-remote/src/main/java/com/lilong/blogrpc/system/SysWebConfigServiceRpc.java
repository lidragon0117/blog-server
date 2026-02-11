package com.lilong.blogrpc.system;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.system.SysWebConfigVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : lilong
 * @date : 2026-02-10 17:46
 * @description :
 */
@FeignClient(name = "blog-system-service", contextId = "sysWebConfigServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysWebConfigServiceRpc {

    @GetMapping("/sys/web/config")
    Result<SysWebConfigVo> selectLastOneWebConfg();

}
