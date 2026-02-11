package com.lilong.blog.matcher;

import com.lilong.blog.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author : lilong
 * @date : 2026-02-09 12:51
 * @description :
 */
public class MyAccessDeniedHandlerMatcher implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        ResponseUtils.writeErrMsg(response, "A0301");
    }
}
