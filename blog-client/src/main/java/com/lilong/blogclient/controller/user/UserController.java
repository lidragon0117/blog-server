package com.lilong.blogclient.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.annotation.OperationLogger;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleVo;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.blogclient.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/1/11
 * @description:
 */
@RestController
@RequestMapping("/client/protal/user")
@RequiredArgsConstructor
@Api(tags = "门户-个人中心")
public class UserController {

    private final UserService userService;

    @PutMapping("/updateProfile")
    @ApiOperation(value = "修改我的资料")
    public Result<Void> updateProfile(@RequestBody SysUserVo user){
        userService.updateProfile(user);
        return Result.success();
    }

    @GetMapping("/comment")
    @ApiOperation(value = "获取我的评论")
    public Result<IPage<CommentListVo>> selectMyComment(){
        return Result.success(userService.selectMyComment());
    }

    @DeleteMapping("/delMyComment/{ids}")
    @ApiOperation(value = "删除我的评论")
    public Result<Void> delMyComment(@PathVariable List<Long> ids){
        return Result.success(userService.delMyComment(ids));
    }

    @GetMapping("/myReply")
    @ApiOperation("获取我的回复")
    public Result<IPage<CommentListVo>> getMyReply() {
        return Result.success(userService.getMyReply());
    }

    @GetMapping("/myLike")
    @ApiOperation(value = "获取我的点赞")
    public Result<IPage<ArticleListVo>> selectMyLike(){
        return Result.success(userService.selectMyLike());
    }

    @GetMapping("/myArticle")
    @ApiOperation(value = "获取我的文章")
    public Result<IPage<ArticleListVo>> selectMyArticle(SysArticleVo article){
        return Result.success(userService.selectMyArticle(article));
    }
}
