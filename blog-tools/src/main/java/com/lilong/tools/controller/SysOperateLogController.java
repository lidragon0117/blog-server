package com.lilong.tools.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.tools.entity.SysOperateLog;
import com.lilong.tools.service.SysOperateLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tool/operateLog")
@RequiredArgsConstructor
@Api(tags = "操作日志管理")
public class SysOperateLogController {

    private final SysOperateLogService sysOperateLogService;

    @GetMapping
    @ApiOperation(value = "获取操作日志列表")
    public Result<IPage<SysOperateLog>> list(SysOperateLog sysOperateLog) {
        return Result.success(sysOperateLogService.listSysOperateLog(sysOperateLog));
    }

    @DeleteMapping("delete/{ids}")
    @ApiOperation(value = "批量删除操作日志")
    @PreAuthorize("@ss.hasPerm('sys:operateLog:delete')")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysOperateLogService.removeBatchByIds(ids);
        return Result.success();
    }
}
