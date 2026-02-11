package com.lilong.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blog.auth.UserAuthCredentials;
import com.lilong.blog.dto.user.SysUserAddAndUpdateDto;
import com.lilong.blog.dto.user.UpdatePwdDTO;
import com.lilong.blog.vo.user.OnlineUserVo;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.system.entity.SysUser;

import java.awt.datatransfer.Clipboard;
import java.util.List;

public interface SysUserService extends IService<SysUser> {
    /**
     * 分页查询用户
     */
    IPage<SysUserVo> listUsers(SysUser sysUser);

    /**
     * 新增用户
     */
    void add(SysUserAddAndUpdateDto user);

    /**
     * 更新用户
     */
    void update(SysUserAddAndUpdateDto user);

    /**
     * 删除用户
     */
    void delete(List<Integer> ids);


    /**
     * 修改密码
     *
     * @param updatePwdDTO 修改密码参数
     */
    void updatePwd(UpdatePwdDTO updatePwdDTO);

    /**
     * 获取个人信息
     *
     * @return
     */
    SysUserProfileVo profile();

    /**
     * 修改个人信息
     *
     * @param user
     */
    void updateProfile(SysUser user);

    /**
     * 锁屏界面验证密码
     *
     * @param password
     * @return
     */
    Boolean verifyPassword(String password);

    /**
     * 重置密码
     *
     * @param user
     * @return
     */
    Boolean resetPassword(SysUser user);

    /**
     * 获取在线用户列表
     *
     * @return
     */
    IPage<OnlineUserVo> getOnlineUserList(String username);

    /**
     * 查询用户信息根据用户名
     *
     * @param userName
     * @return
     */
    UserAuthCredentials getAuthCredentialsByUserName(String userName);
}
