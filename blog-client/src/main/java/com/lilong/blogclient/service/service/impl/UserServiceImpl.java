package com.lilong.blogclient.service.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.vo.article.ArticleListVo;
import com.lilong.blog.vo.article.SysArticleVo;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.blogclient.service.service.UserService;
import com.lilong.blogrpc.act.ArticleServiceRpc;
import com.lilong.blogrpc.act.CommentServiceRpc;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: lilong
 * @date: 2025/1/11
 * @description:
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserServiceRpc sysUserServiceRpc;

    private final CommentServiceRpc commentServiceRpc;

    private final ArticleServiceRpc articleServiceRpc;


    @Override
    public IPage<CommentListVo> selectMyComment() {
        return commentServiceRpc.selectMyComment(PageUtil.getPage(), CurrentUserHelper.getUserId()).getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void delMyComment(List<Long> ids) {
        commentServiceRpc.deleteBatchIds(ids);
        commentServiceRpc.deleteByParentIds(ids);
        return null;
    }

    @Override
    public IPage<ArticleListVo> selectMyLike() {
        return articleServiceRpc.selectMyLike(QueryPageRequest.builder()
                .userId(CurrentUserHelper.getUserId())
                .page(PageUtil.getPage()).build()).getData();
    }

    @Override
    public IPage<CommentListVo> getMyReply() {
        return commentServiceRpc.getMyReply(QueryPageRequest.builder()
                .userId(CurrentUserHelper.getUserId())
                .page(PageUtil.getPage()).build()).getData();
    }

    @Override
    public void updateProfile(SysUserVo user) {
        sysUserServiceRpc.updateProfile(user);
    }

    @Override
    public IPage<ArticleListVo> selectMyArticle(SysArticleVo article) {
        Long userId = CurrentUserHelper.getUserId();
        article.setUserId(userId);

        return articleServiceRpc.selectMyArticle(QueryPageRequest.<SysArticleVo>builder()
                .page(PageUtil.getPage())
                .dto(article)
                .userId(userId)
                .build()).getData();
    }

}
