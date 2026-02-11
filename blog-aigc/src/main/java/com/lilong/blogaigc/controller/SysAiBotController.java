package com.lilong.blogaigc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blogaigc.entity.SysAiBot;
import com.lilong.blogaigc.service.SysAiBotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 16:57
 * @description :
 */
@RestController
@RequestMapping("/aigc/aiBot")
@RequiredArgsConstructor
@Api(tags = "插件管理表管理")
public class SysAiBotController {

    private final SysAiBotService sysAiBotService;

    @GetMapping("/list")
    @ApiOperation(value = "获取插件管理表列表")
    public Result<IPage<SysAiBot>> list(SysAiBot sysAiBot) {
        return Result.success(sysAiBotService.selectPage(sysAiBot));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取插件管理表详情")
    public Result<SysAiBot> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysAiBotService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加插件管理表")
    public Result<Object> add(@RequestBody SysAiBot sysAiBot) {
        return Result.success(sysAiBotService.insert(sysAiBot));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改插件管理表")
    public Result<Object> edit(@RequestBody SysAiBot sysAiBot) {
        return Result.success(sysAiBotService.update(sysAiBot));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除插件管理表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysAiBotService.deleteByIds(ids));
    }
}