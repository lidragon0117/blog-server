package com.lilong.blog.vo.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : lilong
 * @date : 2026-02-10 20:15
 * @description :
 */
@Data
public class SysArticleVo {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "文章封面地址")
    private String cover;

    @ApiModelProperty(value = "文章简介")
    private String summary;

    @ApiModelProperty(value = "文章内容")
    private String content;

    @ApiModelProperty(value = "文章内容md格式")
    private String contentMd;

    @ApiModelProperty(value = "阅读方式 0无需验证 1：评论阅读 2：点赞阅读 3：扫码阅读")
    private Integer readType;

    @ApiModelProperty(value = "是否置顶 0否 1是")
    private Integer isStick;

    @ApiModelProperty(value = "状态 0：下架 1：发布")
    private Integer status;

    @ApiModelProperty(value = "是否原创  0：转载 1:原创")
    private Integer isOriginal;

    @ApiModelProperty(value = "是否首页轮播")
    private Integer isCarousel;

    @ApiModelProperty(value = "是否推荐")
    private Integer isRecommend;

    @ApiModelProperty(value = "转载地址")
    private String originalUrl;

    @ApiModelProperty(value = "文章阅读量")
    private Integer quantity;

    @ApiModelProperty(value = "关键词")
    private String keywords;


    @ApiModelProperty(value = "Ai生成的简短描述")
    private String aiDescribe;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
