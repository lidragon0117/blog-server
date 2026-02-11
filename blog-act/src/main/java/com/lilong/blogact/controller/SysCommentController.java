package com.lilong.blogact.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.JsonUtil;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.SysCommentVO;
import com.lilong.blogact.entity.SysComment;
import com.lilong.blogact.service.SysCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/1/2
 * @description:
 */
@RestController
@Api(tags = "评论管理")
@RequestMapping("/act/comment")
@RequiredArgsConstructor
public class SysCommentController {

    private final SysCommentService sysCommentService;

    @GetMapping("/list")
    @ApiOperation(value = "获取评论列表")
    public Result<Page<SysCommentVO>> list() {
        return Result.success(sysCommentService.selectList());
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除评论")
    public Result<Void> delete(@PathVariable List<Integer> ids) {
        sysCommentService.removeBatchByIds(ids);
        return Result.success();
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增评论")
    public Result<Void> add(@RequestBody SysComment sysComment) {
        sysCommentService.save(sysComment);
        return Result.success();
    }

    @PostMapping("/getMyReply")
    public Result<IPage<CommentListVo>> getMyReply(@RequestBody QueryPageRequest queryPageRequest) {
        return Result.success(sysCommentService.getMyReply(queryPageRequest.getPage(), queryPageRequest.getUserId()));
    }

    @DeleteMapping("/deleteByParentIds/{{ids}}")
    public Result<Void> deleteByParentIds(@PathVariable List<Long> ids) {
        sysCommentService.remove(Wrappers.<SysComment>lambdaQuery()
                .in(SysComment::getParentId, ids));
        return Result.success();
    }

    @PostMapping("/getComments")
    public Result<Page<CommentListVo>> getComments(@RequestBody QueryPageRequest queryPageRequest) {
        Page page = queryPageRequest.getPage();
        SysComment sysComment = JsonUtil.fromJson(JsonUtil.toJsonString(queryPageRequest.getDto()), SysComment.class);
        return Result.success(sysCommentService.getComments(page,sysComment,queryPageRequest.getSortType()));
    }

    @GetMapping("/{commentId}")
    Result<List<CommentListVo>> getChildrenComment(@PathVariable("commentId") Integer commentId){
        return Result.success(sysCommentService.getChildrenComment(commentId));
    }
}
