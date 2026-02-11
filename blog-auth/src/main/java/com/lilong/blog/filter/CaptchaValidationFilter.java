package com.lilong.blog.filter;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.StrUtil;
import com.lilong.blog.constants.auth.SecurityConstants;
import com.lilong.blog.constants.cache.RedisConstants;
import com.lilong.blog.service.RedisService;
import com.lilong.blog.utils.ResponseUtils;
import com.lilong.blog.vo.system.SysConfigVo;
import com.lilong.blogrpc.system.SysConfigServiceRpc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author : lilong
 * @date : 2025-12-19 23:31
 * @description :
 */
public class CaptchaValidationFilter extends OncePerRequestFilter {

    private static final AntPathRequestMatcher LOGIN_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(SecurityConstants.LOGIN_PATH, HttpMethod.POST.name());

    public static final String CAPTCHA_CODE_PARAM_NAME = "captchaCode";
    public static final String CAPTCHA_KEY_PARAM_NAME = "captchaKey";

    private final RedisService redisService;
    private final SysConfigServiceRpc sysConfigServiceRpc;

    public CaptchaValidationFilter(RedisService redisService, SysConfigServiceRpc sysConfigServiceRpc) {
        this.redisService = redisService;
        this.sysConfigServiceRpc = sysConfigServiceRpc;
    }


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 检验登录接口的验证码
        if (LOGIN_PATH_REQUEST_MATCHER.matches(request)) {
            // 请求中的验证码
            String captchaCode = request.getParameter(CAPTCHA_CODE_PARAM_NAME);
            // TODO 兼容没有验证码的版本(线上请移除这个判断)
            if (StrUtil.isBlank(captchaCode)) {
                chain.doFilter(request, response);
                return;
            }
            // 缓存中的验证码
            String verifyCodeKey = request.getParameter(CAPTCHA_KEY_PARAM_NAME);
            String cacheVerifyCode =  redisService.get(
                    StrUtil.format(RedisConstants.Captcha.IMAGE_CODE, verifyCodeKey)
            );
            if (cacheVerifyCode == null) {
                ResponseUtils.writeErrMsg(response,"A0242" ,"用户验证码过期");
            } else {
                // 验证码比对
//               // if (codeGenerator.verify(cacheVerifyCode, captchaCode)) {
//                    chain.doFilter(request, response);
//                } else {
//                    ResponseUtils.writeErrMsg(response, "A0240","验证码错误");
//                }
            }
        } else {
            // 非登录接口放行
            chain.doFilter(request, response);
        }
    }

}