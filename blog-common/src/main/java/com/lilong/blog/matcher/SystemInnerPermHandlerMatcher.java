package com.lilong.blog.matcher;

import cn.hutool.core.util.StrUtil;
import com.lilong.blog.auth.SysUserDetails;
import com.lilong.blog.constants.auth.SystemConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内部鉴权用户信息拦截器
 * <p>
 * 从网关传递的 Header 中提取用户信息，并设置到 SecurityContext
 * </p>
 */
@Slf4j
public class SystemInnerPermHandlerMatcher extends OncePerRequestFilter {

    /**
     * Gateway 传递的用户信息 Header（必须与 Gateway 保持一致）
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 调试：打印所有接收到的 Header
        log.info("========== SystemInnerPermHandlerMatcher 开始 ==========");
        log.info("请求路径: {}", request.getRequestURI());
        java.util.Collections.list(request.getHeaderNames()).forEach(headerName ->
            log.info("Header: {} = {}", headerName, request.getHeader(headerName))
        );
        log.info("=======================================================");

        // 检查是否通过 Gateway 鉴权
        String internalAuth = request.getHeader(SystemConstants.HEADER_INTERNAL_AUTH);
        if ("true".equals(internalAuth)) {
            // 从 Header 提取用户信息
            String userId = request.getHeader(SystemConstants.HEADER_USER_ID);
            String username = request.getHeader(SystemConstants.HEADER_USERNAME);
            String roles = request.getHeader(SystemConstants.HEADER_USER_ROLES);
            String deptId = request.getHeader(SystemConstants.HEADER_USER_DEPT_ID);
            String dataScope = request.getHeader(SystemConstants.HEADER_USER_DATA_SCOPE);

            if (StrUtil.isNotBlank(userId)) {
                // 构建用户详情
                SysUserDetails userDetails = new SysUserDetails();
                userDetails.setUserId(Long.valueOf(userId));
                userDetails.setUsername(username);
                if (StrUtil.isNotBlank(deptId)) {
                    userDetails.setDeptId(Long.valueOf(deptId));
                }
                if (StrUtil.isNotBlank(dataScope)) {
                    userDetails.setDataScope(Integer.valueOf(dataScope));
                }

                // 构建权限集合
                Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                if (StrUtil.isNotBlank(roles)) {
                    authorities = StrUtil.split(roles, ',')
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());
                }
                userDetails.setAuthorities(authorities);

                // 设置到 Security 上下文
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("从 Gateway 提取用户信息成功: {}, 角色: {}", username, roles);
            }
        }

        // 继续后续过滤器链执行
        filterChain.doFilter(request, response);
    }
}
