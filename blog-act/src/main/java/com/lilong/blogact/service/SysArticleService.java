package com.lilong.blogact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blog.dto.article.ArticleQueryDto;
import com.lilong.blog.vo.article.ArticleDetailVo;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleDetailVo;
import com.lilong.blog.vo.article.SysArticleVo;
import com.lilong.blogact.entity.SysArticle;

import java.util.List;

public interface SysArticleService extends IService<SysArticle> {

    /**
     * 分页查询
     *
     * @param articleQueryDto
     * @return
     */
    IPage<ArticleListVo> selectPage(ArticleQueryDto articleQueryDto);

    /**
     * 文章详情
     *
     * @param id
     * @return
     */
    SysArticleDetailVo detail(Integer id);

    /**
     * 新增
     *
     * @param sysArticle
     * @return
     */
    Boolean add(SysArticleDetailVo sysArticle);

    /**
     * 修改
     *
     * @param sysArticle
     * @return
     */
    Boolean update(SysArticleDetailVo sysArticle);


    /**
     * 删除
     *
     * @param ids
     * @return
     */
    Boolean delete(List<Long> ids);

    /**
     * 爬取文章
     *
     * @param url
     */
    void reptile(String url);

    /**
     * 判断用户是否对该文章点过赞
     *
     * @param articleId
     * @param userId
     * @return
     */
    Boolean getUserIsLike(Long articleId, Long userId);

    /**
     * 获取文章年份
     *
     * @return
     */
    List<Integer> getArticleArchive();

    /**
     * 获取文章列表根据年份
     *
     * @param year
     * @return
     */
    List<ArticleListVo> getArticleByYear(Integer year);

    /**
     * 取消喜欢
     *
     * @param articleId
     * @param userId
     */
    void unLike(Long articleId, Long userId);

    /**
     * 喜欢文章
     *
     * @param articleId
     * @param userId
     */
    void like(Long articleId, Long userId);

    /**
     * 查询我喜欢的
     *
     * @param page
     * @param userId
     * @return
     */
    IPage<ArticleListVo> selectMyLike(Page<Object> page, Long userId);

    /**
     * 查询我发布的
     *
     * @param
     */
    IPage<ArticleListVo> selectMyArticle(Page<Object> page, SysArticle sysArticle);

    /**
     * 根据条件查询文章
     *
     * @param conditionField
     * @return
     */
    List<SysArticle> selectArticleList(String conditionField);

    /**
     * 获取文章列表（API接口）
     *
     * @param page
     * @param tagId
     * @param categoryId
     * @param keyword
     * @return
     */
    Page<ArticleListVo> getArticleListApi(Page<Object> page, Integer tagId, Integer categoryId, String keyword);

    /**
     * 获取文章详情
     *
     * @param id
     * @return
     */
    ArticleDetailVo getArtticleDetail(Long id);
}
