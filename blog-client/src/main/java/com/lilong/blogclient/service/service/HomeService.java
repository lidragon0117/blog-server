package com.lilong.blogclient.service.service;

import com.alibaba.fastjson2.JSONObject;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.notice.SysNoticeVo;
import com.lilong.blog.vo.system.SysWebConfigVo;

import java.util.List;
import java.util.Map;

public interface HomeService {

    /**
     * 获取网站配置
     * @return
     */
    Result<SysWebConfigVo> getWebConfig();

    /**
     * 获取热搜
     * @param type
     * @return
     */
    JSONObject getHotSearch(String type);

    /**
     * 添加访问量
     * @return
     */
    void report();


    /**
     * 获取公告
     * @return
     */
    Map<String, List<SysNoticeVo>> getNotice();

}
