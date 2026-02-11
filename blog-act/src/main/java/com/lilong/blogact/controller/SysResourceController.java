package com.lilong.blogact.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.JsonUtil;
import com.lilong.blogact.entity.SysResource;
import com.lilong.blogact.service.SysResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源表 控制器
 */
@RestController
@RequestMapping("/act/resource")
@RequiredArgsConstructor
@Api(tags = "资源表管理")
public class SysResourceController {

    private final SysResourceService sysResourceService;

    @GetMapping("/list")
    @ApiOperation(value = "获取资源表列表")
    public Result<IPage<SysResource>> list(SysResource sysResource) {
        return Result.success(sysResourceService.selectPage(sysResource));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取资源表详情")
    public Result<SysResource> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysResourceService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加资源表")
    public Result<Object> add(@RequestBody SysResource sysResource) {
        return Result.success(sysResourceService.insert(sysResource));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改资源表")
    public Result<Object> edit(@RequestBody SysResource sysResource) {
        return Result.success(sysResourceService.update(sysResource));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除资源表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysResourceService.deleteByIds(ids));
    }

    @GetMapping("/selectPageList")
    @ApiOperation(value = "获取资源表列表-内部")
    public Result<IPage<SysResource>> list(@RequestBody QueryPageRequest queryPageRequest) {
        SysResource sysResource = JsonUtil.fromJson(JsonUtil.toJsonString(queryPageRequest.getDto()), SysResource.class);
        return Result.success(sysResourceService.selectPageList(sysResource,queryPageRequest.getPage()));
    }
}
