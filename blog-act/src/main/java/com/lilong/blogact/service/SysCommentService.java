package com.lilong.blogact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.SysCommentVO;
import com.lilong.blogact.entity.SysComment;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/1/2
 * @description:
 */
public interface SysCommentService extends IService<SysComment> {

    /**
     * 获取评论列表
     *
     * @return
     */
    Page<SysCommentVO> selectList();

    /**
     * 获取我的分页列表
     *
     * @param page
     * @param userId
     * @return
     */
    IPage<CommentListVo> getMyReply(Page<Object> page, Long userId);

    /**
     * 分页查询评论
     * @param page
     * @param sysComment
     * @param sortType
     * @return
     */
    Page<CommentListVo> getComments(Page page, SysComment sysComment, String sortType);
    /**
     * 获取子评论
     * @param commentId
     * @return
     */
    List<CommentListVo> getChildrenComment(Integer commentId);
}
