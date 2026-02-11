package com.lilong.tools.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.annotation.OperationLogger;
import com.lilong.blog.base.Result;
import com.lilong.blog.dto.job.JobLogQuery;
import com.lilong.tools.entity.SysJobLog;
import com.lilong.tools.service.SysJobLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "定时任务日志")
@RestController
@RequestMapping("/tool/monitor/jobLog")
@RequiredArgsConstructor
public class SysJobLogController {

    private final SysJobLogService jobLogService;

    @ApiOperation(value = "获取定时任务日志列表")
    @GetMapping("/list")
    public Result<Page<SysJobLog>> list(JobLogQuery query) {
        return Result.success(jobLogService.selectJobLogPage(query));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除定时任务日志")
    @OperationLogger(value = "删除定时任务日志")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        jobLogService.removeBatchByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清空定时任务日志")
    @OperationLogger(value = "清空定时任务日志")
    public Result<Void> clean() {
        jobLogService.cleanJobLog();
        return Result.success();
    }
}
