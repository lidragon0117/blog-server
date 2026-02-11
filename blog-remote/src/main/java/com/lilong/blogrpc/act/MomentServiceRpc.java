package com.lilong.blogrpc.act;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.moment.MomentPageVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : lilong
 * @date : 2026-02-10 18:26
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "momentServiceRpc", configuration = RpcRequestInterceptor.class)
public interface MomentServiceRpc {
    /**
     * 获取说说列表
     *
     * @param page
     * @return
     */
    @PostMapping("/act/moment/list")
    Result<IPage<MomentPageVo>> selectPage(@RequestBody Page<Object> page);
}
