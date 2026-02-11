package com.lilong.tools.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.annotation.OperationLogger;
import com.lilong.blog.base.Result;
import com.lilong.tools.entity.SysJob;
import com.lilong.tools.quartz.TaskException;
import com.lilong.tools.service.SysJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "定时任务")
@RestController
@RequestMapping("/tool/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;

    @ApiOperation(value = "获取定时任务列表")
    @GetMapping("/list")
    public Result<IPage<SysJob>> list(String jobName, String jobGroup, String status) {
        return Result.success(jobService.selectJobPage(jobName, jobGroup, status));
    }

    @ApiOperation(value = "获取定时任务详情")
    @GetMapping("/{jobId}")
    public Result<SysJob> getInfo(@PathVariable Long jobId) {
        return Result.success(jobService.getById(jobId));
    }

    @PostMapping
    @ApiOperation(value = "新增定时任务")
    @OperationLogger(value = "新增定时任务")
    public Result<Void> add(@RequestBody SysJob job) throws SchedulerException, TaskException {
        jobService.addJob(job);
        return Result.success();
    }

    @PutMapping
    @ApiOperation(value = "修改定时任务")
    @OperationLogger(value = "修改定时任务")
    public Result<Void> edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
        jobService.updateJob(job);
        return Result.success();
    }

    @DeleteMapping("delete/{ids}")
    @ApiOperation(value = "批量删除定时任务")
    @OperationLogger(value = "批量删除定时任务")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        jobService.deleteJob(ids);
        return Result.success();
    }

    @PutMapping("/changeStatus")
    @ApiOperation(value = "修改任务状态")
    @OperationLogger(value = "修改任务状态")
    public Result<Void> changeStatus(@RequestBody SysJob job) throws SchedulerException {
        jobService.changeStatus(job);
        return Result.success();
    }

    @ApiOperation(value = "定时任务立即执行一次")
    @PutMapping("/run")
    public Result<Void> run(@RequestBody SysJob sysJob) {
        jobService.runJob(sysJob);
        return Result.success();
    }
}
