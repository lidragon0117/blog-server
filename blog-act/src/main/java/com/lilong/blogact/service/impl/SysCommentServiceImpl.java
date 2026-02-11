package com.lilong.blogact.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.base.Result;
import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.utils.JsonUtil;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.SysCommentVO;
import com.lilong.blog.vo.user.SysUserVo;
import com.lilong.blogact.entity.SysComment;
import com.lilong.blogact.mapper.SysCommentMapper;
import com.lilong.blogact.service.SysCommentService;
import com.lilong.blogrpc.system.SysUserServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author: lilong
 * @date: 2025/1/2
 * @description:
 */
@Service
@RequiredArgsConstructor
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper, SysComment> implements SysCommentService {

    private final SysCommentMapper sysCommentMapper;

    private final RedisUtil redisUtil;

    private final SysUserServiceRpc sysUserServiceRpc;

    @Override
    public Page<SysCommentVO> selectList() {
        Page<SysCommentVO> sysCommentVOPage = baseMapper.selectPage(PageUtil.getPage());
        if (CollUtil.isNotEmpty(sysCommentVOPage.getRecords())) {
            for (SysCommentVO record : sysCommentVOPage.getRecords()) {
                SysUserVo sysUserVo1 = selectUserByCache(record.getUserId().longValue());
                record.setAvatar(sysUserVo1.getAvatar());
                record.setNickname(sysUserVo1.getNickname());
                Long replyUserId = record.getReplyUserId();
                if (replyUserId != null) {
                    SysUserVo sysUserVo = selectUserByCache(replyUserId.longValue());
                    record.setReplyNickname(sysUserVo.getNickname());
                }
            }
        }
        return sysCommentVOPage;
    }

    @Override
    public IPage<CommentListVo> getMyReply(Page<Object> page, Long userId) {
        IPage<CommentListVo> myReply = sysCommentMapper.getMyReply(page, userId);
        if (CollUtil.isNotEmpty(myReply.getRecords())) {
            for (CommentListVo record : myReply.getRecords()) {
                SysUserVo sysUserVo = selectUserByCache(record.getUserId().longValue());
                SysUserVo replayUser = selectUserByCache(record.getReplyUserId().longValue());
                record.setAvatar(sysUserVo.getAvatar());
                record.setNickname(sysUserVo.getNickname());
                record.setReplyNickname(replayUser.getNickname());
            }
        }
        return myReply;
    }

    @Override
    public Page<CommentListVo> getComments(Page page, SysComment sysComment, String sortType) {
        Page comments = sysCommentMapper.getComments(page, sysComment.getArticleId().intValue(), sortType);
        if (CollUtil.isNotEmpty(comments.getRecords())) {
            for (Object record : comments.getRecords()) {
                CommentListVo detail = (CommentListVo) record;
                SysUserVo sysUserVo = selectUserByCache(detail.getUserId().longValue());
                detail.setAvatar(sysUserVo.getAvatar());
                detail.setNickname(sysUserVo.getNickname());
            }
        }
        return comments;
    }

    @Override
    public List<CommentListVo> getChildrenComment(Integer commentId) {
        List<CommentListVo> childrenComment = sysCommentMapper.getChildrenComment(commentId);
        if (CollUtil.isNotEmpty(childrenComment)) {
            for (CommentListVo children : childrenComment) {
                SysUserVo sysUserVo = selectUserByCache(children.getUserId().longValue());
                children.setAvatar(sysUserVo.getAvatar());
                children.setNickname(sysUserVo.getNickname());
                SysUserVo relayUser = selectUserByCache(children.getReplyUserId());
                children.setReplyNickname(relayUser.getNickname());
            }
        }
        return childrenComment;
    }

    private SysUserVo selectUserByCache(Long userId) {
        Object userCache = redisUtil.get(RedisConstants.USER_INFO_KEY + userId);
        if (Objects.isNull(userCache)) {
            Result<SysUserVo> userInfo = sysUserServiceRpc.selectUserByUserId(userId);
            if (userInfo.isSuccess()) {
                redisUtil.set(RedisConstants.USER_INFO_KEY + userId, userInfo.getData());
                return userInfo.getData();
            }
        }
        return JsonUtil.fromJson(JsonUtil.toJsonString(userCache), SysUserVo.class);

    }
}

