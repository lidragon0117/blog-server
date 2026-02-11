package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.album.SysAlbumVo;
import com.lilong.blog.vo.photo.SysPhotoVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.SpringQueryMap;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 16:37
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "albumServiceRpc", configuration = RpcRequestInterceptor.class)
public interface AlbumServiceRpc {

    @GetMapping("/act/album/all")
    Result<List<SysAlbumVo>> getAlbumList(@SpringQueryMap SysAlbumVo sysAlbumVo);


    @GetMapping("/act/album/{id}")
    Result<SysAlbumVo> selectById(@PathVariable("id") Long id);
}
