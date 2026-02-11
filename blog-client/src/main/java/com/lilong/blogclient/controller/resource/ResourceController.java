package com.lilong.blogclient.controller.resource;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.resource.SysResourceVo;
import com.lilong.blogclient.service.service.ResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/resource")
@RequiredArgsConstructor
@Api(tags = "门户-资源管理")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/list")
    @ApiOperation(value = "资源列表")
    public Result<Page<SysResourceVo>> getResourceList(SysResourceVo sysResource) {
        return Result.success(resourceService.getResourceList(sysResource));
    }

    @PostMapping("/add")
    @ApiOperation(value = "上传资源")
    public Result<Void> add(@RequestBody SysResourceVo sysResource) {
        resourceService.add(sysResource);
        return Result.success();
    }

    @GetMapping("/verify")
    @ApiOperation(value = "校验验证码")
    public Result<SysResourceVo> verify(String code, Long id) {
        return Result.success(resourceService.verify(code, id));
    }


}
