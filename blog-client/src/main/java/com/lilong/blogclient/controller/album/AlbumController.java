package com.lilong.blogclient.controller.album;



import com.lilong.blog.annotation.AccessLimit;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.album.SysAlbumVo;
import com.lilong.blog.vo.photo.SysPhotoVo;
import com.lilong.blogclient.service.service.AlbumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/2/7
 * @description:
 */
@RestController
@RequestMapping("/client/album")
@RequiredArgsConstructor
@Api(tags = "门户-相册管理")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/list")
    @ApiOperation(value = "获取相册列表")
    public Result<List<SysAlbumVo>> getAlbumList() {
        return Result.success(albumService.getAlbumList());
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取相册详情")
    public Result<SysAlbumVo> detail(@PathVariable Long id) {
        return Result.success(albumService.detail(id));
    }

    @GetMapping("/photos/{albumId}")
    @ApiOperation(value = "获取照片列表")
    public Result<List<SysPhotoVo>> getPhotos(@PathVariable Long albumId) {
        return Result.success(albumService.getPhotos(albumId));
    }

    @AccessLimit(count = 5, time = 30)
    @GetMapping("/verify/{id}")
    @ApiOperation(value = "验证相册的密码")
    public Result<Boolean> verify(@PathVariable Long id,String password) {
        return albumService.verify(id,password) ? Result.success(true) : Result.error("密码错误");
    }
}
