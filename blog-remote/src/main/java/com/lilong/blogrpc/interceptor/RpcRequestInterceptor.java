package com.lilong.blogrpc.interceptor;

import cn.hutool.core.util.StrUtil;
import com.lilong.blog.constants.auth.SystemConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author : lilong
 * @date : 2025-11-18 0:36
 * @description : Feign 请求拦截器，转发用户信息到下游服务
 */
@Slf4j
public class RpcRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 设置请求头ak,sk
        requestTemplate.header(SystemConstants.RPC_REQUEST_ACCESS_KEY, "");
        requestTemplate.header(SystemConstants.RPC_REQUEST_SECRET_KEY, "");
        requestTemplate.header(SystemConstants.RPC_REQUEST_INTERNAL_CALL, "true");

        // 转发 Gateway 传递的用户信息 Header
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("转发的 attributes: {}", attributes);
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 转发用户信息 Header
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_USER_ID);
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_USERNAME);
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_USER_ROLES);
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_USER_DEPT_ID);
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_USER_DATA_SCOPE);
            forwardHeader(requestTemplate, request, SystemConstants.HEADER_INTERNAL_AUTH);
        }

        // 获取请求的URL地址
        String url = requestTemplate.feignTarget().url() + requestTemplate.url();
        log.info("请求远程服务的地址: {}", url);
    }

    /**
     * 转发单个 Header
     */
    private void forwardHeader(RequestTemplate requestTemplate, HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (StrUtil.isNotEmpty(headerValue)) {
            requestTemplate.header(headerName, headerValue);
            log.debug("转发 Header: {} = {}", headerName, headerValue);
        }
    }
}