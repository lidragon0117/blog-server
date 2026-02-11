package com.lilong.blogact.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.base.Constants;
import com.lilong.blog.base.Result;
import com.lilong.blog.base.ResultCode;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.dto.article.ArticleQueryDto;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.utils.JsonUtil;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.article.ArticleDetailVo;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleDetailVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.blogact.entity.SysArticle;
import com.lilong.blogact.entity.SysCategory;
import com.lilong.blogact.entity.SysTag;
import com.lilong.blogact.mapper.SysArticleMapper;
import com.lilong.blogact.mapper.SysCategoryMapper;
import com.lilong.blogact.mapper.SysTagMapper;
import com.lilong.blogact.service.SysArticleService;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysArticleServiceImpl extends ServiceImpl<SysArticleMapper, SysArticle> implements SysArticleService {

    private final SysTagMapper sysTagMapper;

    private final SysCategoryMapper sysCategoryMapper;

    private final SysArticleMapper sysArticleMapper;

    private final SysUserServiceRpc sysUserServiceRpc;

    private final RedisUtil redisUtil;

    @Override
    public IPage<ArticleListVo> selectPage(ArticleQueryDto articleQueryDto) {
        IPage<ArticleListVo> articleListVoIPage = baseMapper.selectPageList(PageUtil.getPage(), articleQueryDto);
        if (CollUtil.isNotEmpty(articleListVoIPage.getRecords())) {
            for (ArticleListVo record : articleListVoIPage.getRecords()) {
                SysUserVo sysUserVo = selectUserByCache(record.getUserId());
                record.setAvatar(sysUserVo.getAvatar());
                record.setNickname(sysUserVo.getNickname());
            }
        }
        return articleListVoIPage;
    }

    @Override
    public SysArticleDetailVo detail(Integer id) {
        SysArticle sysArticle = baseMapper.selectById(id);

        SysArticleDetailVo sysArticleDetailVo = new SysArticleDetailVo();
        BeanUtils.copyProperties(sysArticle, sysArticleDetailVo);

        if(sysArticle.getCategoryId()!=null){
            SysCategory sysCategory = sysCategoryMapper.selectById(sysArticle.getCategoryId());
            sysArticleDetailVo.setCategoryName(sysCategory.getName());
        }
        //获取标签
        List<String> tags = sysTagMapper.getTagNameByArticleId(id);
        sysArticleDetailVo.setTags(tags);
        return sysArticleDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysArticleDetailVo sysArticle) {

        SysArticle obj = new SysArticle();
        BeanUtils.copyProperties(sysArticle, obj);
        obj.setUserId(CurrentUserHelper.getUserId());

        //添加分类
        addCategory(sysArticle, obj);
        baseMapper.insert(obj);

        addTags(sysArticle, obj);
        // TODO AI总结描述
//        ThreadUtil.execAsync(() -> {
//            String res = aiUtil.send(obj.getContent() + "请提供一段简短的介绍描述该文章的内容");
//            if (StringUtils.isNotBlank(res)) {
//                obj.setAiDescribe(res);
//                baseMapper.updateById(obj);
//            }
//        });
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysArticleDetailVo sysArticle) {

        SysArticle obj = new SysArticle();
        BeanUtils.copyProperties(sysArticle, obj);

        //没有管理员权限就只能修改自己的文章
        if (!CurrentUserHelper.hasRole(Constants.ADMIN)) {
            SysArticle article = baseMapper.selectById(sysArticle.getId());
            if (article.getUserId() != CurrentUserHelper.getUserId()) {
                throw new ServiceException("只能修改自己的文章");
            }
        }

        addCategory(sysArticle, obj);
        baseMapper.updateById(obj);

        //先删除标签在新增标签
        sysTagMapper.deleteArticleTagsByArticleIds(Collections.singletonList(obj.getId()));
        addTags(sysArticle, obj);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> ids) {

        //没有管理员权限就只能删除自己的文章
        if (!CurrentUserHelper.hasRole(Constants.ADMIN)) {
            List<SysArticle> sysArticles = baseMapper.selectBatchIds(ids);
            for (SysArticle sysArticle : sysArticles) {
                if (sysArticle.getUserId() != CurrentUserHelper.getUserId()) {
                    throw new RuntimeException("只能删除自己的文章");
                }
            }
        }

        baseMapper.deleteBatchIds(ids);
        sysTagMapper.deleteArticleTagsByArticleIds(ids);
        return true;
    }


    @Override
    public void reptile(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements title = document.getElementsByClass("title-article");
            Elements tags = document.getElementsByClass("tag-link");
            Elements content = document.getElementsByClass("article_content");
            if (StringUtils.isBlank(content.toString())) {
                throw new ServiceException(ResultCode.CRAWLING_ARTICLE_FAILED.getDesc());
            }

            //爬取的是HTML内容，需要转成MD格式的内容
            String newContent = content.get(0).toString().replaceAll("<code>", "<code class=\"lang-java\">");
            String markdown = FlexmarkHtmlConverter.builder(new MutableDataSet()).build().convert(newContent)
                    .replace("lang-java", "java");

            SysArticle entity = SysArticle.builder().userId(CurrentUserHelper.getUserId()).contentMd(markdown)
                    .isOriginal(Constants.NO).originalUrl(url)
                    .title(title.get(0).text()).cover("https://api.btstu.cn/sjbz/api.php?lx=dongman&format=images").content(newContent).build();

            baseMapper.insert(entity);
            //为该文章添加标签

            if(CollUtil.isNotEmpty(tags)){
                List<Integer> tagIds = new ArrayList<>();
                tags.forEach(item -> {
                    String tag = item.text();
                    SysTag result = sysTagMapper.selectOne(new LambdaQueryWrapper<SysTag>().eq(SysTag::getName, tag));
                    if (result == null) {
                        result = SysTag.builder().name(tag).build();
                        sysTagMapper.insert(result);
                    }
                    tagIds.add(result.getId());
                });
                sysTagMapper.addArticleTagRelations(entity.getId(), tagIds);
            }
           log.info("文章抓取成功，内容为:" + JSON.toJSONString(entity));
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public Boolean getUserIsLike(Long articleId, Long userId) {
        return sysArticleMapper.getUserIsLike(articleId, userId);
    }

    @Override
    public List<Integer> getArticleArchive() {
        return sysArticleMapper.getArticleArchive();
    }

    @Override
    public List<ArticleListVo> getArticleByYear(Integer year) {
        return sysArticleMapper.getArticleByYear(year);
    }

    @Override
    public void unLike(Long articleId, Long userId) {
        sysArticleMapper.unLike(articleId, userId);
    }

    @Override
    public void like(Long articleId, Long userId) {
        sysArticleMapper.like(articleId, userId);
    }

    @Override
    public IPage<ArticleListVo> selectMyLike(Page<Object> page, Long userId) {
        return sysArticleMapper.selectMyLike(page, userId);
    }

    @Override
    public IPage<ArticleListVo> selectMyArticle(Page<Object> page, SysArticle sysArticle) {
        return sysArticleMapper.selectMyArticle(page, sysArticle);
    }

    @Override
    public List<SysArticle> selectArticleList(String conditionField) {
        LambdaQueryWrapper<SysArticle> wrapper = new LambdaQueryWrapper<SysArticle>()
                .select(SysArticle::getId, SysArticle::getTitle, SysArticle::getCover, SysArticle::getCreateTime)
                .orderByDesc(SysArticle::getCreateTime);
        if ("isCarousel".equals(conditionField)) {
            wrapper.eq(SysArticle::getIsCarousel, 1);
        } else {
            wrapper.eq(SysArticle::getIsRecommend, 1);
        }
        return sysArticleMapper.selectList(wrapper);
    }

    @Override
    public Page<ArticleListVo> getArticleListApi(Page<Object> page, Integer tagId, Integer categoryId, String keyword) {
        Page<ArticleListVo> articlePageList = sysArticleMapper.getArticleListApi(page, tagId, categoryId, keyword);
        if(CollUtil.isNotEmpty(articlePageList.getRecords())){
            articlePageList.getRecords().forEach(item -> {
                SysUserVo sysUserVo = selectUserByCache(item.getUserId());
                item.setAvatar(sysUserVo.getAvatar());
                item.setNickname(sysUserVo.getNickname());
            });
        }
        return articlePageList;
    }

    @Override
    public ArticleDetailVo getArtticleDetail(Long id) {
        ArticleDetailVo articleDetail = sysArticleMapper.getArticleDetail(id);
        if (articleDetail != null) {
            SysUserVo sysUserVo = selectUserByCache(Long.valueOf(articleDetail.getUserId()));
            articleDetail.setNickname(sysUserVo.getNickname());
            articleDetail.setAvatar(sysUserVo.getAvatar());

        }
        return articleDetail;
    }

    private SysUserVo selectUserByCache(Long userId) {
        Object userCache = redisUtil.get(RedisConstants.USER_INFO_KEY + userId);
        if (Objects.isNull(userCache)) {
            Result<SysUserVo> userInfo = sysUserServiceRpc.selectUserByUserId(userId);
            if (userInfo.isSuccess()) {
                redisUtil.set(RedisConstants.USER_INFO_KEY + userId, userInfo.getData());
                return userInfo.getData();
            }
        }
        return JsonUtil.fromJson(JsonUtil.toJsonString(userCache), SysUserVo.class);

    }

    private void addCategory(SysArticleDetailVo sysArticle, SysArticle obj) {
        SysCategory sysCategory = sysCategoryMapper.selectOne(new LambdaQueryWrapper<SysCategory>()
                .eq(SysCategory::getName, sysArticle.getCategoryName()));
        if (sysCategory == null) {
            sysCategory = SysCategory.builder().name(sysArticle.getCategoryName()).build();
            sysCategoryMapper.insert(sysCategory);
        }
        obj.setCategoryId(sysCategory.getId());
    }

    private void addTags(SysArticleDetailVo sysArticle, SysArticle obj) {
        //添加标签
        List<Integer> tagIds = new ArrayList<>();
        for (String tag : sysArticle.getTags()) {
            SysTag sysTag = sysTagMapper.selectOne(new LambdaQueryWrapper<SysTag>().eq(SysTag::getName, tag));
            if (sysTag == null) {
                sysTag = SysTag.builder().name(tag).build();
                sysTagMapper.insert(sysTag);
            }
            tagIds.add(sysTag.getId());
        }
        sysTagMapper.addArticleTagRelations(obj.getId(), tagIds);
    }
}
