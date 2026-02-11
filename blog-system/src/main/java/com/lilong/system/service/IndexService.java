package com.lilong.system.service;

import com.lilong.blog.vo.dashboard.IndexVo;

import java.util.List;
import java.util.Map;


public interface IndexService {

    /**
     * 首页获取顶部数据
     * @return
     */
    IndexVo index();

    /**
     * 获取分类
     * @return
     */
    List<Map<String, Integer>> getCategories();


}
