package com.lilong.blogact.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blogact.entity.SysMessage;
import com.lilong.blogact.service.SysMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/1/2
 * @description:
 */
@RestController
@Api(tags = "留言管理")
@RequestMapping("/act/message")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageService sysMessageService;

    @GetMapping("/list")
    @ApiOperation(value = "获取留言列表")
    public Result<Page<SysMessage>> list() {
        return Result.success(sysMessageService.selectList());
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除留言")
    public Result<Void> delete(@PathVariable List<Integer> ids) {
        sysMessageService.removeBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/selectList")
    @ApiOperation(value = "获取留言列表")
    public Result<List<SysMessage>> selectList() {
        return Result.success(sysMessageService.list());
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增留言")
    public Result<Boolean> add(@RequestBody SysMessage sysMessage) {
        return Result.success(sysMessageService.save(sysMessage));
    }
}
