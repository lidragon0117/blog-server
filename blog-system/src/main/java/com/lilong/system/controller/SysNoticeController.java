package com.lilong.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.system.entity.SysNotice;
import com.lilong.system.service.SysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告 控制器
 */
@RestController
@RequestMapping("/sys/notice")
@RequiredArgsConstructor
@Api(tags = "公告管理")
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    @GetMapping("/list")
    @ApiOperation(value = "获取公告列表")
    public Result<IPage<SysNotice>> list(SysNotice sysNotice) {
        return Result.success(sysNoticeService.selectPage(sysNotice));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取公告详情")
    public Result<SysNotice> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysNoticeService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加公告")
    public Result<Object> add(@RequestBody SysNotice sysNotice) {
        return Result.success(sysNoticeService.insert(sysNotice));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改公告")
    public Result<Object> edit(@RequestBody SysNotice sysNotice) {
        return Result.success(sysNoticeService.update(sysNotice));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除公告")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysNoticeService.deleteByIds(ids));
    }

    @GetMapping("/selectList")
    @ApiOperation(value = "获取公告列表")
    public Result<List<SysNotice>> selectList(SysNotice sysNotice) {
        return Result.success(sysNoticeService.selectList(sysNotice));
    }
}
