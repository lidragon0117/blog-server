package com.lilong.blogrpc.act;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.vo.resource.SysResourceVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author : lilong
 * @date : 2026-02-10 16:55
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "resourceServiceRpc", configuration = RpcRequestInterceptor.class)
public interface ResourceServiceRpc {
    /**
     * 获取资源列表
     *
     * @param queryPageRequest
     * @return
     */
    @PostMapping("/act/resource/selectPageList")
    Result<Page<SysResourceVo>> getResourceList(@RequestBody QueryPageRequest queryPageRequest);

    /**
     * 新增
     *
     * @param sysResource
     * @return
     */
    @PostMapping("/act/resource/add")
    Result<Object> save(@RequestBody SysResourceVo sysResource);

    /**
     * 获取详情
     *
     * @param id
     * @return
     */
    @GetMapping("/act/resource/{id}")
    Result<SysResourceVo> selectById(@PathVariable("id") Long id);

    /**
     * 修改
     *
     * @param sysResource
     */
    @PutMapping("/act/resource/update")
    Result<Object> updateById(@RequestBody SysResourceVo sysResource);
}
