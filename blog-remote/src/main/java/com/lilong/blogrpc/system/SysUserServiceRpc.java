package com.lilong.blogrpc.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.auth.UserAuthCredentials;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.user.OnlineUserVo;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : lilong
 * @date : 2025-12-20 12:24
 * @description : 用户远程过程调用
 */
@FeignClient(name = "blog-system-service", contextId = "sysUserServiceRpc", configuration = RpcRequestInterceptor.class)
public interface SysUserServiceRpc {
    /**
     * 查询用户认证信息
     *
     * @param userName
     * @return
     */
    @GetMapping("/sys/user/selectAuthByUserName")
    Result<UserAuthCredentials> getAuthCredentialsByUsername(@RequestParam String userName);

    /**
     * 获取用户信息
     *
     * @param
     * @return
     */
    @GetMapping("/sys/user/profile")
    Result<SysUserProfileVo> selectCurrentUser();

    /**
     * 获取在线用户列表
     *
     * @param username
     * @return
     */
    @GetMapping("/sys/user/online/list")
    Result<IPage<OnlineUserVo>> getOnlineUserList(@RequestParam(required = false) String username);

    @PostMapping("/sys/user/updateProfile")
    Result<Void> updateProfile(@RequestBody SysUserVo user);

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/sys/user/selectByUserId")
    Result<SysUserVo> selectUserByUserId(@RequestParam("userId") Long userId);
}
