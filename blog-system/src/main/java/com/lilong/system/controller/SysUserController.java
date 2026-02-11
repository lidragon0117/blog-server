package com.lilong.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.annotation.OperationLogger;
import com.lilong.blog.auth.UserAuthCredentials;
import com.lilong.blog.base.Result;
import com.lilong.blog.dto.user.SysUserAddAndUpdateDto;
import com.lilong.blog.dto.user.UpdatePwdDTO;
import com.lilong.blog.vo.user.OnlineUserVo;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.system.entity.SysUser;
import com.lilong.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@Api(tags = "用户管理")
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @ApiOperation(value = "获取用户列表")
    @OperationLogger("获取用户列表")
    public Result<IPage<SysUserVo>> listUsers(SysUser sysUser) {
        return Result.success(sysUserService.listUsers(sysUser));
    }

    @PostMapping
    @OperationLogger("新增用户")
    @ApiOperation(value = "新增用户")
    public Result<Void> addUser(@RequestBody SysUserAddAndUpdateDto sysUserAddDto) {
        sysUserService.add(sysUserAddDto);
        return Result.success();
    }

    @PutMapping
    @OperationLogger("修改用户")
    @ApiOperation(value = "修改用户")
    public Result<Void> update(@RequestBody SysUserAddAndUpdateDto user) {
        sysUserService.update(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{ids}")
    @OperationLogger("批量删除用户")
    @ApiOperation(value = "批量删除用户")
    public Result<Void> delete(@PathVariable List<Integer> ids) {
        sysUserService.delete(ids);
        return Result.success();
    }

    @PutMapping("/updatePwd")
    @ApiOperation(value = "修改密码")
    public Result<Void> updatePwd(@RequestBody UpdatePwdDTO updatePwdDTO) {
        sysUserService.updatePwd(updatePwdDTO);
        return Result.success();
    }

    @GetMapping("/profile")
    @ApiOperation(value = "获取个人信息")
    public Result<SysUserProfileVo> profile() {
        return Result.success(sysUserService.profile());
    }

    @PutMapping("/updProfile")
    @OperationLogger("修改个人信息")
    @ApiOperation(value = "修改个人信息")
    public Result<SysUserProfileVo> updateProfile(@RequestBody SysUser user) {
        sysUserService.updateProfile(user);
        return Result.success();
    }

    @GetMapping("/verifyPassword/{password}")
    @ApiOperation(value = "锁屏界面验证密码")
    public Result<Boolean> verifyPassword(@PathVariable String password) {
        return Result.success(sysUserService.verifyPassword(password));
    }

    @PutMapping("/reset")
    @OperationLogger("重置密码")
    @ApiOperation(value = "重置密码")
    public Result<Boolean> resetPassword(@RequestBody SysUser user) {
        return Result.success(sysUserService.resetPassword(user));
    }

    @GetMapping("/selectAuthByUserName")
    @ApiOperation(value = "查询用户认证信息")
    public Result<UserAuthCredentials> getAuthCredentialsByUserName(@RequestParam String userName) {
        return Result.success(sysUserService.getAuthCredentialsByUserName(userName));
    }

    @GetMapping("/online/list")
    public Result<IPage<OnlineUserVo>> getOnlineUserList(@RequestParam(required = false) String username){
        return Result.success(sysUserService.getOnlineUserList(username));
    }
    @GetMapping("/selectByUserId")
    public Result<SysUser> selectByUserId(@RequestParam Integer userId){
        return Result.success(sysUserService.getById(userId));
    }
}
