package com.lilong.blog.helper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lilong.blog.auth.SysUserDetails;
import com.lilong.blog.constants.CommonConstants;
import com.lilong.blog.constants.auth.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : lilong
 * @date : 2025-12-20 10:00
 * @description :
 */
@Slf4j
public class CurrentUserHelper {
    /**
     * 获取当前登录人信息
     *
     * @return Optional<SysUserDetails>
     */
    public static Optional<SysUserDetails> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof SysUserDetails) {
                return Optional.of((SysUserDetails) principal);
            }
            if (principal instanceof Map) {
                // 关键：保存转换结果！
                SysUserDetails userDetails = JSONUtil.toBean(
                        JSONUtil.toJsonStr(principal),
                        SysUserDetails.class);
                log.debug("Map转换成功: userId={}, username={}",
                        userDetails.getUserId(), userDetails.getUsername());
                return Optional.of(userDetails);
            }
        }
        return Optional.empty();
    }


    /**
     * 获取用户ID
     *
     * @return Long
     */
    public static Long getUserId() {
        return getUser().map(SysUserDetails::getUserId).orElse(null);
    }


    /**
     * 获取用户账号
     *
     * @return String 用户账号
     */
    public static String getUsername() {
        return getUser().map(SysUserDetails::getUsername).orElse(null);
    }


    /**
     * 获取部门ID
     *
     * @return Long
     */
    public static Long getDeptId() {
        return getUser().map(SysUserDetails::getDeptId).orElse(null);
    }

    /**
     * 获取数据权限范围
     *
     * @return Integer
     */
    public static Integer getDataScope() {
        return getUser().map(SysUserDetails::getDataScope).orElse(null);
    }


    /**
     * 获取角色集合
     *
     * @return 角色集合
     */
    public static Set<String> getRoles() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (CollectionUtil.isEmpty(authorities)) {
            return Collections.emptySet();
        }

        return authorities.stream().map(item -> item.getAuthority())
                .filter(item -> item != null)
                .filter(authority -> authority.startsWith(SecurityConstants.ROLE_PREFIX))
                .map(item -> StrUtil.removePrefix(item, SecurityConstants.ROLE_PREFIX))
                .collect(Collectors.toSet());
    }

    /**
     * 是否超级管理员
     * <p>
     * 超级管理员忽视任何权限判断
     */
    public static boolean isRoot() {
        return hasRole("ROOT");
    }

    /**
     * 获取请求中的 Token
     *
     * @return Token 字符串
     */
    public static String getTokenFromRequest() {
        return getVal(HttpHeaders.AUTHORIZATION);
    }

    public static String getCurrentClientIP() {
        return getVal(CommonConstants.HttpConstants.IP);
    }

    public static String getVal(String key) {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (Objects.isNull(servletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request.getHeader(key);
    }

    public static boolean hasRole(String role) {
        Set<String> roles = getRoles();
        return roles.contains(role);
    }
}
