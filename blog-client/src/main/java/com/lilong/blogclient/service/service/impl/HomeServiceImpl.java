package com.lilong.blogclient.service.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lilong.blog.base.Constants;
import com.lilong.blog.base.Result;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.utils.IpUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.notice.SysNoticeVo;
import com.lilong.blog.vo.system.SysWebConfigVo;
import com.lilong.blogclient.service.service.HomeService;
import com.lilong.blogrpc.system.SysNoticeServiceRpc;
import com.lilong.blogrpc.system.SysWebConfigServiceRpc;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final SysWebConfigServiceRpc sysWebConfigServiceRpc;

    private final RedisUtil redisUtil;

    private final SysNoticeServiceRpc sysNoticeServiceRpc;

    @Override
    public Result<SysWebConfigVo> getWebConfig() {

        SysWebConfigVo sysWebConfig = new SysWebConfigVo();
        Object value = redisUtil.get(RedisConstants.WEB_CONFIG_KEY);
        if (value == null) {
            sysWebConfig = sysWebConfigServiceRpc.selectLastOneWebConfg().getData();
        } else {
            sysWebConfig = JSONObject.parseObject(value.toString(), SysWebConfigVo.class);
        }

        //获取浏览量和访问量
        long blogViewsCount = 0;
        long visitorCount = 0;
        if (redisUtil.hasKey(RedisConstants.BLOG_VIEWS_COUNT)) {
            blogViewsCount = Long.parseLong(redisUtil.get(RedisConstants.BLOG_VIEWS_COUNT).toString());
        }
        if (redisUtil.hasKey(RedisConstants.UNIQUE_VISITOR_COUNT)) {
            visitorCount = Long.parseLong(redisUtil.get(RedisConstants.UNIQUE_VISITOR_COUNT).toString());
        }

        return Result.success(sysWebConfig).putExtra("blogViewsCount", blogViewsCount).putExtra("visitorCount", visitorCount);
    }

    @Override
    public JSONObject getHotSearch(String type) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("access-key", "f94be500c45148bc185be24a38c04ad3");
        paramMap.put("secret-key", "27563ca627d5db0d57e831ca4de0f75f");
        String url = "com.lilong/api/resou/v1/" + type;
        String result = HttpUtil.get(url, paramMap);
        return JSONObject.parseObject(result);
    }

    @Override
    public void report() {
        // 获取ip
        String ipAddress = IpUtil.getIp();
        // 通过浏览器解析工具类UserAgent获取访问设备信息
        UserAgent userAgent = IpUtil.getUserAgent(Objects.requireNonNull(IpUtil.getRequest()));
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        // 生成唯一用户标识
        String uuid = ipAddress + browser.getName() + operatingSystem.getName();
        String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes());
        // 判断是否访问
        if (!redisUtil.sIsMember(RedisConstants.UNIQUE_VISITOR, md5)) {
            // 访客量+1
            redisUtil.increment(RedisConstants.UNIQUE_VISITOR_COUNT, 1);
            // 保存唯一标识
            redisUtil.sAdd(RedisConstants.UNIQUE_VISITOR, md5);
        }
        // 访问量+1
        redisUtil.increment(RedisConstants.BLOG_VIEWS_COUNT, 1);
    }

    @Override
    public Map<String, List<SysNoticeVo>> getNotice() {
        SysNoticeVo sysNoticeVo = new SysNoticeVo();
        sysNoticeVo.setIsShow(Constants.YES);
        List<SysNoticeVo> sysNotices = sysNoticeServiceRpc.selectNoticeList(sysNoticeVo).getData();
        return sysNotices.stream()
                .collect(Collectors.groupingBy(SysNoticeVo::getPosition));
    }
}
