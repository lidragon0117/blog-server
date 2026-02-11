package com.lilong.blogclient.config;

import com.lilong.blog.matcher.RpcRequestMatcher;
import com.lilong.blog.matcher.SystemInnerPermHandlerMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // RPC 调用放行（内部服务间调用）
                        .requestMatchers(new RpcRequestMatcher()).permitAll()
                        // ✅ 所有请求都放行，因为鉴权已在 Gateway 完成
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ✅ 添加用户信息提取过滤器（只提取，不做鉴权）
                .addFilterBefore(new SystemInnerPermHandlerMatcher(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
