package com.lilong.blogclient.service.service.impl;


import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.dto.article.ArticlePageQueryDto;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.utils.IpUtil;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.article.*;
import com.lilong.blogclient.service.service.ArticleService;
import com.lilong.blogrpc.act.ArticleServiceRpc;
import com.lilong.blogrpc.act.CategoryServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleServiceRpc articleServiceRpc;

    private final CategoryServiceRpc categoryServiceRpc;

    private final RedisUtil redisUtil;


    @Override
    public Page<ArticleListVo> getArticleList(Integer tagId, Integer categoryId, String keyword) {

        return articleServiceRpc.getArticleListApi(ArticlePageQueryDto.builder()
                .page(PageUtil.getPage())
                .keyword(keyword)
                .categoryId(categoryId)
                .tagId(tagId)
                .build()
        ).getData();
    }

    @Override
    public ArticleDetailVo getArticleDetail(Long id) {
        ArticleDetailVo detailVo = articleServiceRpc.getArticleDetail(id).getData();
        // 判断是否点赞
        Long userId = CurrentUserHelper.getUserId();
        if (userId != null) {
            detailVo.setIsLike(articleServiceRpc.getUserIsLike(id, userId).getData());
        }

        //添加阅读量
        String ip = IpUtil.getIp();
        ThreadUtil.execAsync(() -> {
            Map<Object, Object> map = redisUtil.hGetAll(RedisConstants.ARTICLE_QUANTITY);
            List<String> ipList = (List<String>) map.get(id.toString());
            if (ipList != null) {
                if (!ipList.contains(ip)) {
                    ipList.add(ip);
                }
            } else {
                ipList = new ArrayList<>();
                ipList.add(ip);
            }
            map.put(id.toString(), ipList);
            redisUtil.hSetAll(RedisConstants.ARTICLE_QUANTITY, map);
        });
        return detailVo;
    }

    @Override
    public List<ArchiveListVo> getArticleArchive() {

        List<ArchiveListVo> list = new ArrayList<>();

        List<Integer> years = articleServiceRpc.getArticleArchive().getData();
        for (Integer year : years) {
            //获取文章列表按年分组
            List<ArticleListVo> articleListVos = articleServiceRpc.getArticleByYear(year).getData();
            list.add(new ArchiveListVo(year, articleListVos));
        }
        return list;
    }

    @Override
    public List<CategoryListVo> getArticleCategories() {
        return categoryServiceRpc.getArticleCategories().getData();
    }

    @Override
    public List<ArticleListVo> getCarouselArticle() {
        return getArticlesByCondition("isCarousel");
    }

    @Override
    public List<ArticleListVo> getRecommendArticle() {
        return getArticlesByCondition("isRecommend");
    }

    @Override
    public Boolean like(Long articleId) {
        // 判断是否点赞
        Long userId = CurrentUserHelper.getUserId();
        Boolean isLike = articleServiceRpc.getUserIsLike(articleId, userId).getData();
        if (isLike) {
            // 点过则取消点赞
            articleServiceRpc.unLike(articleId, userId);
        } else {
            articleServiceRpc.like(articleId, userId);
            ThreadUtil.execAsync(() -> {
                //TODO 发送通知事件
//                SysNotifications notifications = SysNotifications.builder()
//                        .title("文章点赞通知")
//                        .articleId(articleId)
//                        .isRead(0)
//                        .type("like")
//                        .fromUserId(StpUtil.getLoginIdAsLong())
//                        .build();
//                notificationsUtil.publish(notifications);
            });
        }
        return true;
    }

    @Override
    public List<SysCategoryVo> getCategoryAll() {
        return categoryServiceRpc.selectAllCategoryList().getData();
    }

    private List<ArticleListVo> getArticlesByCondition(String conditionField) {

        List<SysArticleVo> sysArticles = articleServiceRpc.selectArticleList(conditionField).getData();

        if (sysArticles == null || sysArticles.isEmpty()) {
            return Collections.emptyList();
        }

        return sysArticles.stream().map(item -> ArticleListVo.builder()
                .id(item.getId())
                .cover(item.getCover())
                .title(item.getTitle())
                .createTime(item.getCreateTime())
                .build()).collect(Collectors.toList());
    }
}
