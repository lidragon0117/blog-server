package com.lilong.blog.annotation;

import java.lang.annotation.*;


/**
 * @author : lilong
 * @date : 2025-12-20 13:58
 * @description : RPC服务注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RpcService {
    /**
     * RPC服务注解
     *
     * @return 日志描述
     */
    boolean value() default true;
}