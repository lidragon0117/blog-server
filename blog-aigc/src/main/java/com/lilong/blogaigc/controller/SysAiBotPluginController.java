package com.lilong.blogaigc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blogaigc.entity.SysAiBotPlugin;
import com.lilong.blogaigc.service.SysAiBotPluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 17:03
 * @description : 插件接口管理表管理
 */

@RestController
@RequestMapping("/aigc/bot/plugins")
@RequiredArgsConstructor
@Api(tags = "插件API管理表管理")
public class SysAiBotPluginController {

    private final SysAiBotPluginService sysAiBotPluginService;

    @GetMapping("/list")
    @ApiOperation(value = "获取插件API管理表列表")
    public Result<IPage<SysAiBotPlugin>> list(SysAiBotPlugin sysAiBotPlugin) {
        return Result.success(sysAiBotPluginService.selectPage(sysAiBotPlugin));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取插件API管理表详情")
    public Result<SysAiBotPlugin> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysAiBotPluginService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加插件API管理表")
    public Result<Object> add(@RequestBody SysAiBotPlugin sysAiBotPlugin) {
        return Result.success(sysAiBotPluginService.insert(sysAiBotPlugin));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改插件API管理表")
    public Result<Object> edit(@RequestBody SysAiBotPlugin sysAiBotPlugin) {
        return Result.success(sysAiBotPluginService.update(sysAiBotPlugin));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除插件API管理表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysAiBotPluginService.deleteByIds(ids));
    }
}