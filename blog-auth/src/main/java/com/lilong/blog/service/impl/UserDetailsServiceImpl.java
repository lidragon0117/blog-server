package com.lilong.blog.service.impl;

import com.lilong.blog.auth.SysUserDetails;
import com.lilong.blog.auth.UserAuthCredentials;
import com.lilong.blog.base.Result;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author : lilong
 * @date : 2026-02-07 22:56
 * @description :
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserServiceRpc sysUserServiceRpc;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Result<UserAuthCredentials> userAuthCredentials = sysUserServiceRpc.getAuthCredentialsByUsername(username);
            if (userAuthCredentials == null && !userAuthCredentials.isSuccess()) {
                throw new UsernameNotFoundException(username);
            }
            return new SysUserDetails(userAuthCredentials.getData());
        } catch (Exception e) {
            // 记录异常日志
            log.error("认证异常:{}", e.getMessage());
            // 抛出异常
            throw e;
        }
    }

}

