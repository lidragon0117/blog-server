package com.lilong.blog.matcher;

import com.lilong.blog.utils.ResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * @author : lilong
 * @date : 2026-02-09 12:49
 * @description :
 */
public class MyAuthenticationEntryMatcher implements AuthenticationEntryPoint {
    /**
     * 认证失败处理入口方法
     *
     * @param request       触发异常的请求对象（可用于获取请求头、参数等）
     * @param response      响应对象（用于写入错误信息）
     * @param authException 认证异常对象（包含具体失败原因）
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof BadCredentialsException) {
            // 用户名或密码错误
            ResponseUtils.writeErrMsg(response, "A0210", authException.getMessage());
        } else if (authException instanceof InsufficientAuthenticationException) {
            // 请求头缺失Authorization、Token格式错误、Token过期、签名验证失败
            ResponseUtils.writeErrMsg(response, "A0230", authException.getMessage());
        } else {
            // 其他未明确处理的认证异常（如账户被锁定、账户禁用等）
            ResponseUtils.writeErrMsg(response, "A0200", authException.getMessage());
        }
    }
}
