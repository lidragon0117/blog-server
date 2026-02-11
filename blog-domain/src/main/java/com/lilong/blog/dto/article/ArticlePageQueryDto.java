package com.lilong.blog.dto.article;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : lilong
 * @date : 2026-02-11 14:03
 * @description :
 */
@Data
@Builder
@ApiModel("文章查询DTO")
@AllArgsConstructor
@NoArgsConstructor
public class ArticlePageQueryDto implements Serializable {

    @ApiModelProperty(value = "分页参数")
    private Page<Object> page;

    @ApiModelProperty(value = "标签ID")
    private Integer tagId;

    @ApiModelProperty(value = "分类ID")
    private Integer categoryId;

    @ApiModelProperty(value = "搜索关键词")
    private String keyword;
}

