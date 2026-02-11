package com.lilong.blogrpc.act;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.dto.article.ArticlePageQueryDto;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.vo.article.ArticleDetailVo;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 20:33
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "articleServiceRpc", configuration = RpcRequestInterceptor.class)
public interface ArticleServiceRpc {

    /**
     * 获取文章列表
     *
     * @param articlePageQueryDto
     * @return
     */
    @PostMapping("/act/article/api/list")
    Result<Page<ArticleListVo>> getArticleListApi(@RequestBody ArticlePageQueryDto articlePageQueryDto);

    /**
     * 获取详情
     *
     * @param id
     * @return
     */
    @GetMapping("/act/article/getArticleDetail/{id}")
    Result<ArticleDetailVo> getArticleDetail(@PathVariable Long id);

    /**
     * 判断是否点赞
     *
     * @param articleId
     * @param userId
     * @return
     */
    @GetMapping("/act/article/getUserIsLike")
    Result<Boolean> getUserIsLike(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId);

    /**
     * 获取文章归档
     *
     * @return
     */
    @GetMapping("/act/article/getArticleArchive")
    Result<List<Integer>> getArticleArchive();

    /**
     * 获取文章列表按年分组
     *
     * @param year
     * @return
     */
    @GetMapping("/act/article/getArticleByYear")
    Result<List<ArticleListVo>> getArticleByYear(@RequestParam("year") Integer year);

    /**
     * 取消喜欢
     *
     * @param articleId
     * @param userId
     * @return
     */
    @GetMapping("/act/article/unLike")
    Result<Void> unLike(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId);

    /**
     * 喜欢
     *
     * @param articleId
     * @param userId
     * @return
     */
    @GetMapping("/act/article/like")
    Result<Void> like(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId);

    /**
     * 查询我的喜欢
     *
     * @param queryPageRequest
     * @return
     **/
    @PostMapping("/act/article/myLike")
    Result<IPage<ArticleListVo>> selectMyLike(@RequestBody QueryPageRequest queryPageRequest);

    /**
     * 查询我的文章
     *
     * @param queryPageRequest
     * @return
     */
    @PostMapping("/act/article/myArticle")
    Result<IPage<ArticleListVo>> selectMyArticle(@RequestBody QueryPageRequest<SysArticleVo> queryPageRequest);

    /**
     * 根据条件查询文章列表
     *
     * @param conditionField
     * @return
     */
    @PostMapping("/act/article/condition")
    Result<List<SysArticleVo>> selectArticleList(@RequestParam("conditionField") String conditionField);
}
