package com.lilong.blogrpc.system;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.system.SysConfigVo;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : lilong
 * @date : 2026-02-09 15:03
 * @description : 系统配置远程过程调用
 */
@FeignClient(name = "blog-system-service", contextId = "sysConfigServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysConfigServiceRpc {
    /**
     * 根据key获取系统配置
     *
     * @param key
     * @return
     */
    @GetMapping("/getConfigByKey/{key}")
    Result<SysConfigVo> selectSysConfigByKey(@PathVariable("key") String key);
}
