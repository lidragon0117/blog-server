package com.lilong.blog.service;

import com.lilong.blog.dto.Captcha;
import com.lilong.blog.dto.auth.AuthenticationToken;
import com.lilong.blog.dto.auth.EmailRegisterDto;
import com.lilong.blog.dto.auth.LoginDTO;
import com.lilong.blog.dto.user.LoginUserInfo;
import jakarta.servlet.http.HttpServletResponse;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.zhyd.oauth.model.AuthCallback;

import java.io.IOException;

/**
 * @author : lilong
 * @date : 2026-02-07 22:46
 * @description :
 */
public interface AuthService {


    /**
     * 用户登录
     */
    AuthenticationToken login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    LoginUserInfo getLoginUserInfo(String source);

    /**
     * 发送注册邮箱验证码
     *
     * @param email
     * @return
     */
    Boolean sendEmailCode(String email);

    /**
     * 邮箱账号注册
     *
     * @param dto
     * @return
     */
    Boolean register(EmailRegisterDto dto);

    /**
     * 邮箱账号重置密码
     *
     * @param dto
     * @return
     */
    Boolean forgot(EmailRegisterDto dto);

    /**
     * 获取微信扫码登录验证码
     *
     * @return
     */
    String getWechatLoginCode();


    /**
     * 验证微信是否扫码登录
     *
     * @param loginCode
     * @return
     */
    LoginUserInfo getWechatIsLogin(String loginCode);

    /**
     * 微信公众号登录
     *
     * @param message
     * @return
     */
    String wechatLogin(WxMpXmlMessage message);

    /**
     * 获取第三方授权地址
     *
     * @param source
     * @return
     */
    String renderAuth(String source);

    /**
     * 第三方授权登录
     *
     * @param source
     * @param httpServletResponse
     */
    void authLogin(AuthCallback callback, String source, HttpServletResponse httpServletResponse) throws IOException;

    /**
     * 小程序登录
     *
     * @param code
     * @return
     */
    LoginUserInfo appletLogin(String code);

    /**
     * 获取滑块验证码
     *
     * @return
     */
    Captcha getCaptcha();

    void logout();

    void forceLogout(String token);
}
