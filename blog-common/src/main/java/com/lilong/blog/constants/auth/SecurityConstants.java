package com.lilong.blog.constants.auth;

/**
 * @author : lilong
 * @date : 2025-12-19 22:11
 * @description :安全模块常量
 */
public interface SecurityConstants {
    /**
     * 登录路径
     */
    String LOGIN_PATH = "/auth/login";

    /**
     * JWT Token 前缀
     */
    String BEARER_TOKEN_PREFIX  = "Bearer ";

    /**
     * 角色前缀，用于区分 authorities 角色和权限， ROLE_* 角色 、没有前缀的是权限
     */
    String ROLE_PREFIX = "ROLE_";
}
