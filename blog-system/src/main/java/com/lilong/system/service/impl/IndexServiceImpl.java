package com.lilong.system.service.impl;

import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.dashboard.ContributionData;
import com.lilong.blog.vo.dashboard.IndexVo;
//import com.lilong.system.mapper.SysArticleMapper;
//import com.lilong.system.mapper.SysMessageMapper;
import com.lilong.system.mapper.SysUserMapper;
import com.lilong.system.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SysUserMapper sysUserMapper;

//    private final SysArticleMapper sysArticleMapper;
//
//    private final SysMessageMapper sysMessageMapper;

    private final RedisUtil redisUtil;

    @Override
    public IndexVo index() {
        Long userCount = sysUserMapper.selectCount(null);
        Long articleCount =0L; //sysArticleMapper.selectCount(null);
        Long messageCount = 0L;//sysMessageMapper.selectCount(null);

        int visitCount = 0;
        Object e = redisUtil.get(RedisConstants.BLOG_VIEWS_COUNT);
        if (e != null) {
            visitCount = Integer.parseInt(e.toString());
        }

        List<ContributionData> list =new ArrayList<>(); //sysArticleMapper.getThisYearContributionData();

        return IndexVo.builder()
                .articleCount(articleCount)
                .userCount(userCount)
                .messageCount(messageCount)
                .visitCount(visitCount)
                .contributionData(list)
                .build();
    }

    @Override
    public List<Map<String, Integer>> getCategories() {
        List<Map<String, Integer>> list =new ArrayList<>();// sysArticleMapper.selectCountByCategory();
        return list;
    }
}
