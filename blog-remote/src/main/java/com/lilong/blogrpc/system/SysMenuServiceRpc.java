package com.lilong.blogrpc.system;

import com.lilong.blog.base.Result;
import com.lilong.blog.remote.system.QueryPermissionRequest;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-09 17:58
 * @description :
 */
@FeignClient(name = "blog-system-service", contextId = "sysMenuServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysMenuServiceRpc {

    @PostMapping("/sys/menu/permissionList")
    Result<List<String>> permissionList(@RequestBody QueryPermissionRequest queryPermissionRequest);
}
