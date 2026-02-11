package com.lilong.blogrpc.act;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.base.Result;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.CommentRpcVO;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 17:06
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "commentServiceRpc", configuration = RpcRequestInterceptor.class)
public interface CommentServiceRpc {
    /**
     * 获取子评论
     *
     * @param commentId
     * @return
     */
    @GetMapping("/act/comment/{commentId}")
    Result<List<CommentListVo>> getChildrenComment(@PathVariable("commentId") Integer commentId);

    /**
     * 获取评论列表
     *
     * @return
     */
    @PostMapping("/act/comment/getComments")
    Result<Page<CommentListVo>> getComments(@RequestBody QueryPageRequest queryPageRequest);

    /**
     * 添加评论
     *
     * @param sysComment
     * @return
     */
    @PostMapping("/act/comment/add")
    Result<Void> add(@RequestBody CommentRpcVO sysComment);

    /**
     * 查询我的评论
     *
     * @param page
     * @param userId
     * @return
     */
    @PostMapping("/act/comment/selectMyComment")
    Result<IPage<CommentListVo>> selectMyComment(@RequestParam("page") Page<Object> page, @RequestParam("userId") Long userId);

    /**
     * 根据ID删除评论
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/act/comment/delete/{ids}")
    Result<Void> deleteBatchIds(@PathVariable List<Long> ids);

    /**
     * 根据父类ID删除评论
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/act/comment/deleteByParentIds/{{ids}}")
    Result<Void> deleteByParentIds(@PathVariable List<Long> ids);

    /**
     * 获取我的回复
     *
     * @param queryPageRequest
     * @return
     */
    @PostMapping("/act/comment/getMyReply")
    Result<IPage<CommentListVo>> getMyReply(@RequestBody QueryPageRequest queryPageRequest);
}
