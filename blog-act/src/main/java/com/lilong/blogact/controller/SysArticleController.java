package com.lilong.blogact.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.dto.article.ArticlePageQueryDto;
import com.lilong.blog.dto.article.ArticleQueryDto;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.JsonUtil;
import com.lilong.blog.vo.article.ArticleDetailVo;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleDetailVo;
import com.lilong.blog.vo.article.SysArticleVo;
import com.lilong.blogact.entity.SysArticle;
import com.lilong.blogact.service.SysArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "文章管理")
@RequestMapping("/act/article")
@RequiredArgsConstructor
public class SysArticleController {

    private final SysArticleService sysArticleService;

    @GetMapping("/list")
    @ApiOperation(value = "文章列表")
    public Result<IPage<ArticleListVo>> list(ArticleQueryDto articleQueryDto) {
        return Result.success(sysArticleService.selectPage(articleQueryDto));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "文章详情")
    public Result<SysArticleDetailVo> detail(@PathVariable Integer id) {
        return Result.success(sysArticleService.detail(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增文章")
    public Result<Boolean> add(@RequestBody SysArticleDetailVo sysArticle) {
        return Result.success(sysArticleService.add(sysArticle));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改文章")
    public Result<Boolean> update(@RequestBody SysArticleDetailVo sysArticle) {
        return Result.success(sysArticleService.update(sysArticle));
    }

    @PutMapping("/updateStatus")
    @ApiOperation(value = "修改状态")
    public Result<Boolean> updateStatus(@RequestBody SysArticle sysArticle) {
        return Result.success(sysArticleService.updateById(sysArticle));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除文章")
    public Result<Boolean> delete(@PathVariable List<Long> ids) {
        return Result.success(sysArticleService.delete(ids));
    }

    @GetMapping("/reptile")
    @ApiOperation(value = "爬取文章")
    public Result<Void> reptile(String url) {
        sysArticleService.reptile(url);
        return Result.success();
    }

    @GetMapping("/getUserIsLike")
    @ApiOperation(value = "判断用户是否点赞")
    public Result<Boolean> getUserIsLike(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId) {
        return Result.success(sysArticleService.getUserIsLike(articleId, userId));
    }

    @GetMapping("/getArticleArchive")
    @ApiOperation(value = "获取文章归档")
    public Result<List<Integer>> getArticleArchive() {
        return Result.success(sysArticleService.getArticleArchive());
    }


    @GetMapping("/getArticleByYear")
    @ApiOperation(value = "获取文章列表按年分组")
    public Result<List<ArticleListVo>> getArticleByYear(@RequestParam("year") Integer year) {
        return Result.success(sysArticleService.getArticleByYear(year));
    }

    @GetMapping("/unLike")
    @ApiOperation(value = "取消喜欢文章")
    public Result<Void> unLike(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId) {
        sysArticleService.unLike(articleId, userId);
        return Result.success();
    }


    @GetMapping("/like")
    @ApiOperation(value = "喜欢文章")
    public Result<Void> like(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId) {
        sysArticleService.like(articleId, userId);
        return Result.success();
    }

    @PostMapping("/myLike")
    @ApiOperation(value = "查询我的喜欢")
    public Result<IPage<ArticleListVo>> selectMyLike(@RequestBody QueryPageRequest queryPageRequest) {
        return Result.success(sysArticleService.selectMyLike(queryPageRequest.getPage(), queryPageRequest.getUserId()));
    }

    @PostMapping("/myArticle")
    @ApiOperation(value = "查询我的文章")
    public Result<IPage<ArticleListVo>> selectMyArticle(@RequestBody QueryPageRequest<SysArticleVo> queryPageRequest) {
        SysArticle sysArticle = JsonUtil.fromJson(JsonUtil.toJsonString(queryPageRequest.getDto()), SysArticle.class);
        return Result.success(sysArticleService.selectMyArticle(queryPageRequest.getPage(), sysArticle));
    }

    @PostMapping("/condition")
    @ApiOperation(value = "根据字段查询文章")
    public Result<List<SysArticle>> selectArticleList(@RequestParam("conditionField") String conditionField) {
        return Result.success(sysArticleService.selectArticleList(conditionField));
    }

    @PostMapping("/api/list")
    @ApiOperation(value = "获取文章列表API")
    public Result<Page<ArticleListVo>> getArticleListApi(@RequestBody ArticlePageQueryDto articlePageQueryDto) {
        return Result.success(sysArticleService.getArticleListApi(
                articlePageQueryDto.getPage(),
                articlePageQueryDto.getTagId(),
                articlePageQueryDto.getCategoryId(),
                articlePageQueryDto.getKeyword())
        );
    }

    @GetMapping("/getArticleDetail/{id}")
    @ApiOperation(value = "获取文章详情")
    public Result<ArticleDetailVo> getArtticleDetail(@PathVariable("id") Long id) {
        return Result.success(sysArticleService.getArtticleDetail(id));
    }
}
