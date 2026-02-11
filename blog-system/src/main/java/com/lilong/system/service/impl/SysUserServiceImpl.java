package com.lilong.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.auth.UserAuthCredentials;
import com.lilong.blog.base.Constants;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.dto.user.SysUserAddAndUpdateDto;
import com.lilong.blog.dto.user.UpdatePwdDTO;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.user.OnlineUserVo;
import com.lilong.blog.vo.user.SysUserProfileVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.system.entity.SysUser;
import com.lilong.system.mapper.SysRoleMapper;
import com.lilong.system.mapper.SysUserMapper;
import com.lilong.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysRoleMapper roleMapper;

    private final RedisUtil redisUtil;

    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<SysUserVo> listUsers(SysUser sysUser) {
        return baseMapper.selectUserPage(PageUtil.getPage(),sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserAddAndUpdateDto SysUserAddAndUpdateDto) {
        // 检查用户名是否已存在
        SysUserVo user = SysUserAddAndUpdateDto.getUser();
        if (baseMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        save(BeanUtil.toBean(user, SysUser.class));

        //保存角色信息
        roleMapper.addRoleUser(user.getId(), SysUserAddAndUpdateDto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserAddAndUpdateDto user) {
        // 检查用户是否存在
        if (getById(user.getUser().getId()) == null) {
            throw new RuntimeException("用户不存在");
        }
        updateById(BeanUtil.toBean(user.getUser(), SysUser.class));

        //修改角色 先删除角色再新增
        roleMapper.deleteRoleByUserId(Collections.singletonList(user.getUser().getId()));
        roleMapper.addRoleUser(user.getUser().getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids) {
        removeBatchByIds(ids);
        roleMapper.deleteRoleByUserId(ids);
    }


    @Override
    public void updatePwd(UpdatePwdDTO updatePwdDTO) {

        SysUser user = this.getById(CurrentUserHelper.getUserId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if(!CurrentUserHelper.hasRole(Constants.ADMIN) && !user.getId().equals(CurrentUserHelper.getUserId())) {
            throw new ServiceException("只能修改自己的密码！");
        }

        if (!BCrypt.checkpw(updatePwdDTO.getOldPassword(), user.getPassword())) {
            throw new ServiceException("旧密码错误");
        }

        user.setPassword(BCrypt.hashpw(updatePwdDTO.getNewPassword(),BCrypt.gensalt()));
        this.updateById(user);
    }

    @Override
    public SysUserProfileVo profile() {

        SysUser sysUser = baseMapper.selectById(CurrentUserHelper.getUserId());
        sysUser.setPassword(null);
        //获取角色
        List<String> roles = roleMapper.selectRolesByUserId(sysUser.getId());

        return SysUserProfileVo.builder().sysUser(BeanUtil.toBean(sysUser, SysUserVo.class)).roles(roles).build();
    }

    @Override
    public void updateProfile(SysUser user) {
        baseMapper.updateById(user);
    }

    @Override
    public Boolean verifyPassword(String password) {
        SysUser user = baseMapper.selectById(CurrentUserHelper.getUserId());
        return BCrypt.checkpw(password, user.getPassword());
    }

    @Override
    public Boolean resetPassword(SysUser user) {
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        baseMapper.updateById(user);
        return true;
    }

    @Override
    public IPage<OnlineUserVo> getOnlineUserList(String username) {
        Integer pageNum = PageUtil.getPageQuery().getPageNum();
        Integer pageSize = PageUtil.getPageQuery().getPageSize();

        // 返回数据对象
        Collection<String> keys = redisUtil.keys(RedisConstants.LOGIN_TOKEN.concat( "*"));

        List<OnlineUserVo> totalList = new ArrayList<>();
        for (String key : keys) {
            Object userObj = redisUtil.get(key);
            OnlineUserVo onlineUser = JSONUtil.toBean(userObj.toString(), OnlineUserVo.class);
            if (StringUtils.isNotBlank(username)) {
                if (onlineUser.getUsername().contains(username)) {
                    totalList.add(onlineUser);
                }
                continue;
            }
            totalList.add(onlineUser);
        }

        //根据时间排序
        totalList.sort((o1, o2) -> o2.getLastLoginTime().compareTo(o1.getLastLoginTime()));

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = totalList.size() - fromIndex > pageSize ? fromIndex + pageSize : totalList.size();
        List<OnlineUserVo> records = totalList.subList(fromIndex, toIndex);

        IPage<OnlineUserVo> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(totalList.size());
        return page;
    }

    @Override
    public UserAuthCredentials getAuthCredentialsByUserName(String userName) {
        SysUser sysUser = sysUserMapper.selectByUsername(userName);
        UserAuthCredentials userAuthCredentials = new UserAuthCredentials();
        userAuthCredentials.setUserId(Long.valueOf(sysUser.getId()));
        userAuthCredentials.setUsername(sysUser.getUsername());
        userAuthCredentials.setNickname(sysUser.getNickname());
        //TODO 部门和数据权限需要完善
        userAuthCredentials.setDeptId(null);
        userAuthCredentials.setDataScope(null);
        userAuthCredentials.setPassword(sysUser.getPassword());
        userAuthCredentials.setStatus(sysUser.getStatus());
        userAuthCredentials.setRoles(new HashSet<>(roleMapper.selectRolesCodeByUserId(sysUser.getId())));
        return userAuthCredentials;
    }
}
