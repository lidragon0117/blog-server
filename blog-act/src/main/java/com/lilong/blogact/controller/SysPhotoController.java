package com.lilong.blogact.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blogact.entity.SysPhoto;
import com.lilong.blogact.service.SysPhotoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 照片 控制器
 */
@RestController
@RequestMapping("/act/photo")
@RequiredArgsConstructor
@Api(tags = "照片管理")
public class SysPhotoController {

    private final SysPhotoService sysPhotoService;

    @GetMapping("/list")
    @ApiOperation(value = "获取照片列表")
    public Result<IPage<SysPhoto>> list(SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.selectPage(sysPhoto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取照片详情")
    public Result<SysPhoto> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysPhotoService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加照片")
    public Result<Object> add(@RequestBody SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.insert(sysPhoto));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改照片")
    public Result<Object> edit(@RequestBody SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.update(sysPhoto));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除照片")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysPhotoService.deleteByIds(ids));
    }

    @PutMapping("/move/{ids}")
    @ApiOperation(value = "移动照片")
    public Result<Object> move(@PathVariable List<Long> ids, @RequestParam Long albumId) {
        return Result.success(sysPhotoService.move(ids, albumId));
    }

    @GetMapping("/selectListByAlbumId")
    @ApiOperation(value = "获取照片列表")
    public Result<List<SysPhoto>> selectListByAlbumId(@RequestParam("albumId") Long albumId) {
        return Result.success(sysPhotoService.list(new LambdaQueryWrapper<SysPhoto>()
                .eq(SysPhoto::getAlbumId, albumId)
                .orderByAsc(SysPhoto::getSort)
                .orderByDesc(SysPhoto::getRecordTime)));
    }

}
