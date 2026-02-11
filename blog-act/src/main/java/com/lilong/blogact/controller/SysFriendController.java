package com.lilong.blogact.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blogact.entity.SysFriend;
import com.lilong.blogact.service.SysFriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 友情链接 控制器
 */
@RestController
@Api(tags = "友情链接管理")
@RequestMapping("/act/friend")
@RequiredArgsConstructor
public class SysFriendController {

    private final SysFriendService sysFriendService;

    @GetMapping("/list")
    @ApiOperation(value = "友情链接列表")
    public Result<IPage<SysFriend>> list(SysFriend sysFriend) {
        return Result.success(sysFriendService.selectPage(sysFriend));
    }

    @PostMapping("add")
    @ApiOperation(value = "新增友情链接")
    public Result<Object> add(@RequestBody SysFriend sysFriend) {
        return Result.success(sysFriendService.save(sysFriend));
    }

    @PutMapping("update")
    @ApiOperation(value = "修改友情链接")
    public Result<Object> update(@RequestBody SysFriend sysFriend) {
        return Result.success(sysFriendService.update(sysFriend));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除友情链接")
    public Result<Object> delete(@PathVariable List<Integer> ids) {
        return Result.success(sysFriendService.removeBatchByIds(ids));
    }

    @GetMapping("/innerList")
    @ApiOperation(value = "获取友情链接列表")
    public Result<List<SysFriend>> innerList() {
        return Result.success(sysFriendService.selectList());
    }


    @GetMapping("/selectByUrl")
    @ApiOperation(value = "根据url查询友情链接")
    public Result<SysFriend> selectByUrl(String url) {
        return Result.success(sysFriendService.getOne(new LambdaQueryWrapper<SysFriend>().eq(SysFriend::getUrl, url)));
    }
}
