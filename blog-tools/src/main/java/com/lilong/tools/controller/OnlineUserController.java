package com.lilong.tools.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.user.OnlineUserVo;
import com.lilong.blogrpc.auth.AuthServiceRpc;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author: lilong
 * @date: 2025/1/3
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/tool/online")
@Api(tags = "在线用户")
public class OnlineUserController {

    private final SysUserServiceRpc sysUserServiceRpc;

    private final AuthServiceRpc authServiceRpc;


    @GetMapping("/list")
    @ApiOperation(value = "获取在线用户列表")
    public Result<IPage<OnlineUserVo>> getOnlineUserList(@RequestParam(required = false) String username) {
        return Result.success(sysUserServiceRpc.getOnlineUserList(username).getData());
    }


    @ApiOperation(value = "强制踢出")
    @GetMapping("/forceLogout/{token}")
    public Result<Void> forceLogout(@PathVariable String token) {
        authServiceRpc.forceLogout(token);
        return Result.success();
    }

}
