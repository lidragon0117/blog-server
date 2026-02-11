package com.lilong.blogact.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.article.CategoryListVo;
import com.lilong.blogact.entity.SysCategory;
import com.lilong.blogact.service.SysCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类表 控制器
 */
@RestController
@Api(tags = "分类管理")
@RequiredArgsConstructor
@RequestMapping("/act/category")
public class SysCategoryController {

    private final SysCategoryService sysCategoryService;

    @GetMapping("/list")
    @ApiOperation(value = "分类列表")
    public Result<IPage<SysCategory>> list(SysCategory sysCategory) {
        return Result.success(sysCategoryService.selectPage(sysCategory));
    }

    @GetMapping("/all")
    @ApiOperation(value = "获取全部分类")
    public Result<List<SysCategory>> selectAllList() {
        return Result.success(sysCategoryService.selectList(null));
    }

    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result<Object> add(@RequestBody SysCategory sysCategory) {
        return Result.success(sysCategoryService.insert(sysCategory));
    }

    @PutMapping
    @ApiOperation(value = "修改分类")
    public Result<Object> edit(@RequestBody SysCategory sysCategory) {
        return Result.success(sysCategoryService.update(sysCategory));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除分类")
    public Result<Object> remove(@PathVariable List<Integer> ids) {
        return Result.success(sysCategoryService.deleteByIds(ids));
    }


    @GetMapping("/selectArticleCategories")
    @ApiOperation(value = "获取文章分类")
    public Result<List<CategoryListVo>> getArticleCategories() {
        return Result.success(sysCategoryService.getArticleCategories());
    }
}
