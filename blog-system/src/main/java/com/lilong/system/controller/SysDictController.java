package com.lilong.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.annotation.OperationLogger;
import com.lilong.blog.base.Result;
import com.lilong.system.entity.SysDict;
import com.lilong.system.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "字典管理")
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    @GetMapping
    @ApiOperation(value = "获取字典列表")
    public Result<IPage<SysDict>> getDictList(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) Integer status) {
        return Result.success(sysDictService.getDictPageList(name,status));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加字典")
    @OperationLogger(value = "添加字典")
    public Result<Void> addDict(@RequestBody SysDict dict) {
        sysDictService.addDict(dict);
        return Result.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改字典")
    @OperationLogger(value = "修改字典")
    public Result<Void> updateDict(@RequestBody SysDict dict) {
        sysDictService.updateDict(dict);
        return Result.success();
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除字典")
    @OperationLogger(value = "删除字典")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysDictService.removeBatchByIds(ids);
        return Result.success();
    }
}
