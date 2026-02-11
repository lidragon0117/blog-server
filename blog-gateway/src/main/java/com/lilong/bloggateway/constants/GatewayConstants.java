package com.lilong.bloggateway.constants;

/**
 * @author : lilong
 * @date : 2025-12-22 15:00
 * @description : Gateway 内部常量定义
 * <p>
 * 说明：为了避免 Gateway 依赖业务模块（webchat-common），
 * 将必要的常量在 Gateway 内部定义一份。
 * </p>
 */
public class GatewayConstants {

    /**
     * 安全相关常量
     */
    public static class Security {
        /**
         * JWT Token 前缀
         */
        public static final String BEARER_TOKEN_PREFIX = "Bearer ";

        /**
         * JWT Claims 常量
         */
        public static class JwtClaims {
            /**
             * 令牌类型
             */
            public static final String TOKEN_TYPE = "tokenType";

            /**
             * 用户ID
             */
            public static final String USER_ID = "userId";

            /**
             * 部门ID
             */
            public static final String DEPT_ID = "deptId";

            /**
             * 数据权限范围
             */
            public static final String DATA_SCOPE = "dataScope";

            /**
             * 权限(角色Code)集合
             */
            public static final String AUTHORITIES = "authorities";

            /**
             * 主题（用户名）
             */
            public static final String SUBJECT = "sub";

            /**
             * JWT ID（用于黑名单）
             */
            public static final String JWT_ID = "jti";
        }

        /**
         * Gateway 传递给下游服务的 Header 常量
         */
        public static class Headers {
            /**
             * 用户ID
             */
            public static final String X_USER_ID = "X-User-Id";

            /**
             * 用户名
             */
            public static final String X_USERNAME = "X-Username";

            /**
             * 用户角色列表（逗号分隔）
             */
            public static final String X_USER_ROLES = "X-User-Roles";

            /**
             * 部门ID
             */
            public static final String X_USER_DEPT_ID = "X-User-DeptId";

            /**
             * 数据权限范围
             */
            public static final String X_USER_DATA_SCOPE = "X-User-DataScope";

            /**
             * 内部认证标记（固定值 true）
             */
            public static final String X_INTERNAL_AUTH = "X-Internal-Auth";
        }
    }

    /**
     * Redis Key 常量
     */
    public static class Redis {
        /**
         * Token 黑名单 Key
         * <p>
         * 格式：auth:token:blacklist:{jti}
         * </p>
         */
        public static final String BLACKLIST_TOKEN = "auth:token:blacklist:%s";
    }
}
