package com.lilong.blog.matcher;

import com.lilong.blog.constants.auth.SystemConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author : lilong
 * @date : 2026-02-09 12:49
 * @description :
 */
public class RpcRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        // TODO 更加完善权限校验
        return "true".equals(request.getHeader(SystemConstants.RPC_REQUEST_INTERNAL_CALL));
    }
}
