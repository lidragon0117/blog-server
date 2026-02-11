package com.lilong.blogrpc.auth;

import com.lilong.blog.base.Result;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : lilong
 * @date : 2026-02-09 23:47
 * @description :
 */
@FeignClient(name = "blog-auth-service", contextId = "authServiceRpc", configuration = RpcRequestInterceptor.class)
public interface AuthServiceRpc {
    /**
     * 强制踢出
     *
     * @return
     */
    @PostMapping("/auth/forceLogout")
    public Result<Void> forceLogout(@RequestParam String token);
}
