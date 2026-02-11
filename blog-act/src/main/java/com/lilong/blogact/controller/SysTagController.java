package com.lilong.blogact.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.tag.TagListVo;
import com.lilong.blogact.entity.SysTag;
import com.lilong.blogact.service.SysTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签表 控制器
 */
@RestController
@RequestMapping("/act/tag")
@RequiredArgsConstructor
@Api(tags = "标签管理")
public class SysTagController {

    private final SysTagService sysTagService;

    @GetMapping("/list")
    @ApiOperation(value = "标签列表")
    public Result<IPage<SysTag>> list(SysTag sysTag) {
        return Result.success(sysTagService.selectPage(sysTag));
    }

    @PostMapping
    @ApiOperation(value = "新增标签")
    public Result<Object> add(@RequestBody SysTag sysTag) {
        return Result.success(sysTagService.insert(sysTag));
    }

    @PutMapping
    @ApiOperation(value = "修改标签")
    public Result<Object> edit(@RequestBody SysTag sysTag) {
        return Result.success(sysTagService.update(sysTag));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除标签")
    public Result<Object> remove(@PathVariable List<Integer> ids) {
        return Result.success(sysTagService.deleteByIds(ids));
    }

    @GetMapping("/selectTagList")
    @ApiOperation(value = "查询标签")
    Result<List<TagListVo>> selectTagList(){
        return Result.success(sysTagService.selectTagList());
    }
}
