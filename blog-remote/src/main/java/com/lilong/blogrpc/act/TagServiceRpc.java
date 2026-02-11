package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.tag.TagListVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.datatransfer.Clipboard;
import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 20:26
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "tagServiceRpc", configuration = RpcRequestInterceptor.class)
public interface TagServiceRpc {
    /**
     * 查询所有标签
     * @return
     */
    @GetMapping("/act/tag/selectTagList")
    Result<List<TagListVo>> selectTagList();
}
