package com.lilong.blog.utils;

import com.lilong.blog.annotation.RpcService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @author : lilong
 * @date : 2026-02-09 12:46
 * @description :
 */
@Slf4j
@Component
public class RealApiRpcUtil {
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 实时判断请求是否是RPC接口
     */
    public boolean isRpcService(HttpServletRequest request) {
        try {
            HandlerExecutionChain handlerChain = requestMappingHandlerMapping.getHandler(request);
            if (handlerChain == null || handlerChain.getHandler() == null) {
                return false;
            }
            Object handler = handlerChain.getHandler();
            if (!(handler instanceof HandlerMethod)) {
                return false;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 2. 检查方法是否标注了@RpcService注解
            Method method = handlerMethod.getMethod();
            RpcService rpcService = AnnotationUtils.findAnnotation(method, RpcService.class);

            boolean isRpc = rpcService != null && rpcService.value();
            log.debug("实时检测 - 路径: {}, 方法: {}, 是否是RPC: {}", request.getRequestURI(), method.getName(), isRpc);
            return isRpc;
        } catch (Exception e) {
            log.warn("实时检测RPC接口失败: {}", e.getMessage());
            return false;
        }
    }
}
