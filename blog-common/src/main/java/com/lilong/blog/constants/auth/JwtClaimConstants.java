package com.lilong.blog.constants.auth;

/**
 * @author : lilong
 * @date : 2025-12-19 22:55
 * @description : JWT Claims声明常量
 */
public interface JwtClaimConstants {

    /**
     * 令牌类型
     */
    String TOKEN_TYPE = "tokenType";

    /**
     * 用户ID
     */
    String USER_ID = "userId";

    /**
     * 部门ID
     */
    String DEPT_ID = "deptId";

    /**
     * 数据权限
     */
    String DATA_SCOPE = "dataScope";

    /**
     * 权限(角色Code)集合
     */
    String AUTHORITIES = "authorities";
}
