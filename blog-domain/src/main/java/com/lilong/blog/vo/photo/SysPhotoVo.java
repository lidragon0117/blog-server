package com.lilong.blog.vo.photo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author : lilong
 * @date : 2026-02-10 16:39
 * @description :
 */
@Data
public class SysPhotoVo {

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "相册id")
    private Long albumId;

    @ApiModelProperty(value = "图片地址")
    private String url;

    @ApiModelProperty(value = "图片描述")
    private String description;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate recordTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
