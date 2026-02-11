package com.lilong.system.controller;



import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.system.entity.SysConfig;
import com.lilong.system.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置表 控制器
 */
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
@Api(tags = "参数配置表管理")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/list")
    @ApiOperation(value = "获取参数配置表列表")
    public Result<IPage<SysConfig>> list(SysConfig sysConfig) {
        return Result.success(sysConfigService.selectPage(sysConfig));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取参数配置表详情")
    public Result<SysConfig> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysConfigService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加参数配置表")
    public Result<Object> add(@RequestBody SysConfig sysConfig) {
        return Result.success(sysConfigService.insert(sysConfig));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改参数配置表")
    public Result<Object> edit(@RequestBody SysConfig sysConfig) {
        return Result.success(sysConfigService.update(sysConfig));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除参数配置表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysConfigService.deleteByIds(ids));
    }

    @GetMapping("/getConfigByKey/{key}")
    @ApiOperation(value = "根据key获取参数配置详情")
    public Result<SysConfig> selectConfigByKey(@PathVariable("key") String key) {
        return Result.success(sysConfigService.selectConfigByKey(key));
    }
}
