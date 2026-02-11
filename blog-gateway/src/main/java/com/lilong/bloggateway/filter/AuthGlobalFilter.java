package com.lilong.bloggateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import com.lilong.bloggateway.constants.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author : lilong
 * @date : 2025-12-22 10:00
 * @description : Gateway 统一鉴权过滤器
 * <p>
 * 职责：
 * 1. 验证 JWT Token（本地验证，无 RPC 开销）
 * 2. 检查 Token 黑名单（预留扩展点）
 * 3. 提取用户信息并传递给下游服务
 * </p>
 * <p>
 * 白名单策略：
 * - 登录接口（/auth/login、/auth/captcha）必须放行
 * - 其他公开接口由业务服务自行控制（通过 @PermitAll 等注解）
 * </p>
 * <p>
 * 说明：
 * - 不依赖 webchat-common 模块，保持 Gateway 独立性
 * - 使用 Gateway 内部常量（GatewayConstants）
 * - 与 SSO 服务保持相同的 JWT 密钥即可验证 Token
 * </p>
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * JWT 签名密钥（与 SSO 服务保持一致）
     */
    @Value("${security.session.jwt.secret-key}")
    private String secretKey;

    /**
     * 认证策略类型（jwt 或 redis-token）
     */
    @Value("${security.session.type:jwt}")
    private String sessionType;

    /**
     * 登录相关的公开路径（必须放行，否则无法登录）
     */
    private static final Set<String> AUTH_PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/getCaptcha",
            "/sys/config/getConfigByKey/slider_verify_switch",
            "/api/sys/config/getConfigByKey/slider_verify_switch"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // 1. 登录接口白名单检查（必须放行）
        if (isAuthPublicPath(path)) {
            log.debug("登录接口，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少认证令牌: {}", path);
            return unauthorized(exchange, "A0230", "访问令牌缺失");
        }

        // 3. 根据认证策略类型验证 Token
        try {
            UserInfo userInfo;
            if ("jwt".equalsIgnoreCase(sessionType)) {
                // JWT 模式：本地验证（性能最优）
                userInfo = validateJwtToken(token);
            } else {
                // Redis-Token 模式：需要调用 SSO 服务验证（预留，暂不实现）
                // TODO: 后续如需支持 Redis-Token 模式，可在此处调用 SSO 的 PermissionRpc
                return unauthorized(exchange, "A0230", "暂不支持 Redis-Token 模式，请使用 JWT 模式");
            }

            // 4. TODO: 检查 Token 黑名单（预留扩展点，用于强制踢人功能）
             if (isTokenBlacklisted(token)) {
                 return unauthorized(exchange, "A0230", "令牌已失效");
             }

            // 5. 提取用户信息并传递给下游服务
            ServerHttpRequest.Builder builder = request.mutate()
                    .header(GatewayConstants.Security.Headers.X_USER_ID, String.valueOf(userInfo.userId))
                    .header(GatewayConstants.Security.Headers.X_USERNAME, userInfo.username)
                    .header(GatewayConstants.Security.Headers.X_USER_ROLES, String.join(",", userInfo.roles))
                    .header(GatewayConstants.Security.Headers.X_INTERNAL_AUTH, "true");

            // deptId 和 dataScope 可能为 null，需要判断
            if (Objects.nonNull(userInfo.deptId)) {
                builder.header(GatewayConstants.Security.Headers.X_USER_DEPT_ID, String.valueOf(userInfo.deptId));
            }
            if (Objects.nonNull(userInfo.dataScope)) {
                builder.header(GatewayConstants.Security.Headers.X_USER_DATA_SCOPE, String.valueOf(userInfo.dataScope));
            }

            ServerHttpRequest mutatedRequest = builder.build();

            log.info("========== Gateway 鉴权成功 ==========");
            log.info("用户: {}, 角色: {}", userInfo.username, userInfo.roles);
            log.info("传递的 Header - {}: {}", GatewayConstants.Security.Headers.X_USER_ID, userInfo.userId);
            log.info("传递的 Header - {}: {}", GatewayConstants.Security.Headers.X_USERNAME, userInfo.username);
            log.info("传递的 Header - deptId: {}, dataScope: {}", userInfo.deptId, userInfo.dataScope);
            log.info("====================================");
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage(), e);
            return unauthorized(exchange, "A0230", "访问令牌无效或已过期");
        }
    }

    /**
     * 验证 JWT Token（本地验证）
     *
     * @param token JWT Token
     * @return 用户信息
     */
    private UserInfo validateJwtToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);

        // 验证签名和过期时间
        boolean isValid = jwt.setKey(secretKey.getBytes()).validate(0);
        if (!isValid) {
            throw new IllegalArgumentException("Token 验证失败");
        }

        // 解析用户信息
        JSONObject payloads = jwt.getPayloads();
        UserInfo userInfo = new UserInfo();
        userInfo.userId = payloads.getLong(GatewayConstants.Security.JwtClaims.USER_ID);
        userInfo.username = payloads.getStr(GatewayConstants.Security.JwtClaims.SUBJECT);
        userInfo.deptId = payloads.getLong(GatewayConstants.Security.JwtClaims.DEPT_ID);
        userInfo.dataScope = payloads.getInt(GatewayConstants.Security.JwtClaims.DATA_SCOPE);

        // 解析角色列表（JSONArray 转换为 Set）
        JSONArray authoritiesArray = payloads.getJSONArray(GatewayConstants.Security.JwtClaims.AUTHORITIES);
        userInfo.roles = authoritiesArray != null
            ? new HashSet<>(authoritiesArray.toList(String.class))
            : new HashSet<>();

        return userInfo;
    }

    /**
     * 检查是否为登录相关的公开路径
     *
     * @param path 请求路径
     * @return 是否为公开路径
     */
    private boolean isAuthPublicPath(String path) {
        return AUTH_PUBLIC_PATHS.contains(path);
    }

    /**
     * 提取 Token
     *
     * @param request 请求
     * @return Token（不含 Bearer 前缀）
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(GatewayConstants.Security.BEARER_TOKEN_PREFIX)) {
            return bearerToken.substring(GatewayConstants.Security.BEARER_TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 返回未授权响应
     *
     * @param exchange 交换器
     * @param code     错误码
     * @param message  错误信息
     * @return Mono
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
                {"code":"%s","message":"%s","timestamp":%d}
                """, code, message, System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * TODO: 检查 Token 是否在黑名单中（预留扩展点）
     * <p>
     * 使用场景：
     * 1. 强制踢人
     * 2. 用户修改密码后使旧 Token 失效
     * 3. 管理员封禁用户
     * </p>
     *
     * @param token Token
     * @return 是否在黑名单中
     */
    @SuppressWarnings("unused")
    private boolean isTokenBlacklisted(String token) {
        // TODO: 后续实现时，可从 Redis 检查 Token 黑名单
        return false;
    }

    @Override
    public int getOrder() {
        // 确保在 MetricsFilter 之前执行
        return -100;
    }

    /**
     * 用户信息（内部类）
     */
    private static class UserInfo {
        Long userId;
        String username;
        Long deptId;
        Integer dataScope;
        Set<String> roles;
    }
}
