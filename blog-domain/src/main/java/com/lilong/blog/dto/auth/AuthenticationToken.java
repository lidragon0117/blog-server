package com.lilong.blog.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2025-12-19 22:03
 * @description : 认证令牌响应对象
 */
@Data
@Builder
public class AuthenticationToken {
    /**
     * 令牌类型
     */
    private String tokenType;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 过期时间(单位：秒)
     */
    private Integer expiresIn;
}
