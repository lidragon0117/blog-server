package com.lilong.blogrpc.system;

import com.lilong.blog.base.Result;
import com.lilong.blog.remote.system.QueryRoleRequest;
import com.lilong.blog.vo.SysRoleVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-09 17:50
 * @description :
 */
@FeignClient(name = "blog-system-service", contextId = "sysRoleServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysRoleServiceRpc {

    /**
     * 根据条件获取角色列表
     *
     * @param queryRoleRequest
     * @return
     */
    @PostMapping("/sys/role/selectRoleCodesByUserId")
    Result<List<String>> selectRoleCodesByUserId(@RequestBody QueryRoleRequest queryRoleRequest);
}
