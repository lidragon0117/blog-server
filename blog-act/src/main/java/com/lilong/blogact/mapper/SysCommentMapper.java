package com.lilong.blogact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilong.blog.vo.comment.CommentListVo;
import com.lilong.blog.vo.comment.SysCommentVO;
import com.lilong.blogact.entity.SysComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper接口
 */
@Mapper
public interface SysCommentMapper extends BaseMapper<SysComment> {

    Page<CommentListVo> getComments(@Param("page") Page<Object> page, @Param("articleId") Integer articleId,
                                     @Param("sortType") String sortType);

    List<CommentListVo> getChildrenComment(Integer id);

    Page<SysCommentVO> selectPage(Page<SysCommentVO> page);

    IPage<CommentListVo> selectMyComment(@Param("page") Page<Object> page,@Param("userId")  long userId);

    IPage<CommentListVo> getMyReply(@Param("page") Page<Object> page,@Param("userId")  long userId);

}
