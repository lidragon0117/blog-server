package com.lilong.blogact.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blogact.entity.SysMoment;
import com.lilong.blogact.service.SysMomentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 说说 控制器
 */
@RestController
@RequestMapping("/act/moment")
@RequiredArgsConstructor
@Api(tags = "说说管理")
public class SysMomentController {

    private final SysMomentService sysMomentService;

    @GetMapping("/list")
    @ApiOperation(value = "获取说说列表")
    public Result<IPage<SysMoment>> list(SysMoment sysMoment) {
        return Result.success(sysMomentService.selectPage(sysMoment));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加说说")
    public Result<Object> add(@RequestBody SysMoment sysMoment) {
        return Result.success(sysMomentService.add(sysMoment));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改说说")
    public Result<Object> edit(@RequestBody SysMoment sysMoment) {
        return Result.success(sysMomentService.updateById(sysMoment));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除说说")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysMomentService.removeByIds(ids));
    }
}
