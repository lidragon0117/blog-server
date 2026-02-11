package com.lilong.blogclient.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.remote.act.QueryPageRequest;
import com.lilong.blog.utils.IpUtil;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.utils.SensitiveUtil;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.CommentRpcVO;
import com.lilong.blogclient.service.service.CommentService;
import com.lilong.blogrpc.act.CommentServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentServiceRpc commentServiceRpc;

    @Override
    public Page<CommentListVo> getComments(Integer articleId, String sortType) {

        CommentListVo queryPageRequest = new CommentListVo();
        queryPageRequest.setArticleId(articleId);

        Page<CommentListVo> page = commentServiceRpc.getComments(QueryPageRequest.<CommentListVo>builder()
                .userId(CurrentUserHelper.getUserId())
                .dto(queryPageRequest)
                .sortType(sortType)
                .page(PageUtil.getPage()).build()).getData();
        //获取所有子评论
        page.getRecords().forEach(commentListVo -> {
            List<CommentListVo> children = commentServiceRpc.getChildrenComment(commentListVo.getId()).getData();
            commentListVo.setChildren(children);
        });
        return page;
    }

    @Override
    public void add(CommentRpcVO sysComment) {

        String ip = IpUtil.getIp();
        sysComment.setIp(ip);
        sysComment.setIpSource(IpUtil.getIp2region(ip));
        sysComment.setUserId(CurrentUserHelper.getUserId());
        sysComment.setContent(SensitiveUtil.filter(sysComment.getContent()));

        commentServiceRpc.add(sysComment);

        //TODO 消息通知走MQ
//        ThreadUtil.execAsync(() -> {
//            //发送通知事件
//            SysNotifications notifications = SysNotifications.builder()
//                    .title(sysComment.getReplyUserId() != null ? "评论回复通知" : "新评论通知")
//                    .message(sysComment.getContent())
//                    .articleId(sysComment.getArticleId())
//                    .isRead(0)
//                    .type("comment")
//                    .userId(sysComment.getReplyUserId())
//                    .fromUserId(sysComment.getUserId())
//                    .build();
//            notificationsUtil.publish(notifications);
//        });
    }
}
