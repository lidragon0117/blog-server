package com.lilong.blogact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilong.blog.vo.tag.TagListVo;
import com.lilong.blogact.entity.SysTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签表 Mapper接口
 */
@Mapper
public interface SysTagMapper extends BaseMapper<SysTag> {

    List<TagListVo> getTagsApi();

    List<String> getTagNameByArticleId(Integer id);

    List<TagListVo> getTagByArticleId(Long id);


    void deleteArticleTagsByArticleIds(List<Long> ids);

    void addArticleTagRelations(@Param("articleId") Long articleId, @Param("tagIds") List<Integer> tagIds);

}
