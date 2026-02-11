package com.lilong.blogclient.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.CommentRpcVO;

public interface CommentService {

    /**
     * 获取评论列表
     *
     * @param articleId
     * @return
     */

    Page<CommentListVo> getComments(Integer articleId, String sortType);

    /**
     * 新增评论
     *
     * @param sysComment
     * @return
     */
    void add(CommentRpcVO sysComment);
}
