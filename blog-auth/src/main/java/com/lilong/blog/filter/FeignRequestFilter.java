package com.lilong.blog.filter;

import com.lilong.blog.constants.auth.SystemConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author : lilong
 * @date : 2025-12-20
 * @description : Feign内部调用请求过滤器，用于跳过安全认证
 */
@Component
@Order(1)
public class FeignRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 检查是否是内部Feign调用
        String internalCallHeader = request.getHeader(SystemConstants.RPC_REQUEST_INTERNAL_CALL);

        if ("true".equals(internalCallHeader)) {
            // 如果是内部调用，直接放行，跳过安全认证
            filterChain.doFilter(request, response);
            return;
        }
        // 非内部调用，继续正常的过滤器链
        filterChain.doFilter(request, response);
    }
}