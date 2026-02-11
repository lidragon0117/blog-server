package com.lilong.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lilong.blog.base.Result;
import com.lilong.system.entity.SysWebConfig;
import com.lilong.system.service.SysWebConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "网站配置管理")
@RequestMapping("/sys/web")
@RequiredArgsConstructor
public class SysWebConfigController {

    private final SysWebConfigService sysWebConfigService;

    @GetMapping("/config")
    @ApiOperation(value = "获取网站配置")
    public Result<SysWebConfig> getWebConfig() {
        return Result.success(sysWebConfigService.getOne(new LambdaQueryWrapper<SysWebConfig>().last("limit 1")));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改网站配置")
    public Result<Void> update(@RequestBody SysWebConfig sysWebConfig) {
        sysWebConfigService.update(sysWebConfig);
        return Result.success();
    }
}
