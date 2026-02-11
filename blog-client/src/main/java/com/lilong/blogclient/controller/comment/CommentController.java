package com.lilong.blogclient.controller.comment;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.base.Result;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.CommentRpcVO;
import com.lilong.blog.vo.comment.SysCommentVO;
import com.lilong.blogclient.service.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/comment")
@Api(tags = "门户-评论管理")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/list")
    @ApiOperation(value = "获取文章评论列表")
    public Result<IPage<CommentListVo>> getComments(Integer articleId, String sortType) {
        return Result.success(commentService.getComments(articleId,sortType));
    }

    @PostMapping("/add")
    @ApiOperation(value = "获取文章评论列表")
    public Result<Void> add(@RequestBody CommentRpcVO sysComment) {
        commentService.add(sysComment);
        return Result.success();
    }
}
