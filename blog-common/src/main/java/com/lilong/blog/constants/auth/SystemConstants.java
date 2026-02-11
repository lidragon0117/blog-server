package com.lilong.blog.constants.auth;

/**
 * @author : lilong
 * @date : 2025-12-20 10:11
 * @description :
 */
public interface SystemConstants {
    /**
     * 根节点ID
     */
    Long ROOT_NODE_ID = 0L;

    /**
     * 系统默认密码
     */
    String DEFAULT_PASSWORD = "123456";

    /**
     * 超级管理员角色编码
     */
    String ROOT_ROLE_CODE = "ROOT";


    /**
     * 系统配置 IP的QPS限流的KEY
     */
    String SYSTEM_CONFIG_IP_QPS_LIMIT_KEY = "IP_QPS_THRESHOLD_LIMIT";


    String HEADER_USER_ID = "X-User-Id";
    String HEADER_USERNAME = "X-Username";
    String HEADER_USER_ROLES = "X-User-Roles";
    String HEADER_USER_DEPT_ID = "X-User-DeptId";
    String HEADER_USER_DATA_SCOPE = "X-User-DataScope";
    String HEADER_INTERNAL_AUTH = "X-Internal-Auth";
    String RPC_REQUEST_ACCESS_KEY = "rpc-req-access-key";
    String RPC_REQUEST_SECRET_KEY = "rpc-req-secret-key";
    String RPC_REQUEST_INTERNAL_CALL = "rpc-req-internal-call";
}
