package com.lilong.blogact.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.dto.feedback.SysFeedbackQueryDto;
import com.lilong.blog.vo.feedback.SysFeedbackVo;
import com.lilong.blogact.entity.SysFeedback;
import com.lilong.blogact.service.SysFeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈表 控制器
 */
@RestController
@RequestMapping("/act/feedback")
@RequiredArgsConstructor
@Api(tags = "反馈管理")
public class SysFeedbackController {

    private final SysFeedbackService sysFeedbackService;


    @GetMapping("/list")
    @ApiOperation(value = "获取反馈列表")
    public Result<IPage<SysFeedbackVo>> list(SysFeedbackQueryDto feedbackQueryDto) {
        return Result.success(sysFeedbackService.selectPage(feedbackQueryDto));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加反馈")
    public Result<Object> add(@RequestBody SysFeedback sysFeedback) {
        return Result.success(sysFeedbackService.insert(sysFeedback));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改反馈")
    public Result<Object> update(@RequestBody SysFeedback sysFeedback) {
        return Result.success(sysFeedbackService.update(sysFeedback));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除反馈")
    public Result<Object> delete(@PathVariable List<Long> ids) {
        return Result.success(sysFeedbackService.removeBatchByIds(ids));
    }
}
