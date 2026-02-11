package com.lilong.blog.configuration;

import cn.hutool.core.util.ArrayUtil;
import com.lilong.blog.filter.CaptchaValidationFilter;
import com.lilong.blog.filter.TokenAuthenticationFilter;
import com.lilong.blog.matcher.MyAccessDeniedHandlerMatcher;
import com.lilong.blog.matcher.MyAuthenticationEntryMatcher;
import com.lilong.blog.matcher.RpcRequestMatcher;
import com.lilong.blog.permission.TokenManager;
import com.lilong.blog.service.RedisService;
import com.lilong.blog.service.impl.UserDetailsServiceImpl;
import com.lilong.blog.utils.RealApiRpcUtil;
import com.lilong.blogrpc.system.SysConfigServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author : lilong
 * @date : 2025-12-19 23:01
 * @description :
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Autowired
    private RedisService redisService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenManager tokenManager;
    // private final UserService userService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
//    @Autowired
//    private CodeGenerator codeGenerator;
    //  private final ConfigService configService;
    @Autowired
    private final SecurityProperties securityProperties;
    @Autowired
    private RealApiRpcUtil realApiRpcUtil;

    @Autowired
    private SysConfigServiceRpc sysConfigServiceRpc;

    /**
     * 配置安全过滤链 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests(requestMatcherRegistry -> {
                            // 配置无需登录即可访问的公开接口
                            String[] ignoreUrls = securityProperties.getIgnoreUrls();
                            if (ArrayUtil.isNotEmpty(ignoreUrls)) {
                                requestMatcherRegistry.requestMatchers(ignoreUrls).permitAll();
                            }
                            requestMatcherRegistry.requestMatchers(new RpcRequestMatcher()).permitAll();
                            // 其他所有请求需登录后访问
                            requestMatcherRegistry.anyRequest().authenticated();
                        }
                )
                .exceptionHandling(configurer ->
                        configurer
                                .authenticationEntryPoint(new MyAuthenticationEntryMatcher()) // 未认证异常处理器
                                .accessDeniedHandler(new MyAccessDeniedHandlerMatcher()) // 无权限访问异常处理器
                )

                // 禁用默认的 Spring Security 特性，适用于前后端分离架构
                .sessionManagement(configurer ->
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态认证，不使用 Session
                )
                .csrf(AbstractHttpConfigurer::disable)      // 禁用 CSRF 防护，前后端分离无需此防护机制
                .formLogin(AbstractHttpConfigurer::disable) // 禁用默认的表单登录功能，前后端分离采用 Token 认证方式
                .httpBasic(AbstractHttpConfigurer::disable) // 禁用 HTTP Basic 认证，避免弹窗式登录
                // 禁用 X-Frame-Options 响应头，允许页面被嵌套到 iframe 中
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 限流过滤器
                // .addFilterBefore(new RateLimiterFilter(redisService, configService), UsernamePasswordAuthenticationFilter.class)
                // 验证码校验过滤器
                .addFilterBefore(new CaptchaValidationFilter(redisService,sysConfigServiceRpc), UsernamePasswordAuthenticationFilter.class)
                // 验证和解析过滤器
                .addFilterBefore(new TokenAuthenticationFilter(tokenManager), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 配置Web安全自定义器，以忽略特定请求路径的安全性检查。
     * <p>
     * 该配置用于指定哪些请求路径不经过Spring Security过滤器链。通常用于静态资源文件。
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            String[] unsecuredUrls = securityProperties.getUnsecuredUrls();
            if (ArrayUtil.isNotEmpty(unsecuredUrls)) {
                web.ignoring().requestMatchers(unsecuredUrls);
            }
        };
    }

    /**
     * 默认密码认证的 Provider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }
}
