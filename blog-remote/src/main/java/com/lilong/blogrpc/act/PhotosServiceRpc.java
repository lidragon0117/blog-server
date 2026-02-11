package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.photo.SysPhotoVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 22:06
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "photosServiceRpc", configuration = RpcRequestInterceptor.class)
public interface PhotosServiceRpc {
    /**
     * 获取照片列表
     *
     * @param albumId
     * @return
     */
    @GetMapping("/act/photo/selectListByAlbumId")
    Result<List<SysPhotoVo>> getPhotos(@RequestParam("albumId") Long albumId);
}
