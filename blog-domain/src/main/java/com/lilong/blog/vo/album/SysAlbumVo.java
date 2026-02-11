package com.lilong.blog.vo.album;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : lilong
 * @date : 2026-02-10 16:38
 * @description :
 */
@Data
public class SysAlbumVo {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "封面图")
    private String cover;

    @ApiModelProperty(value = "相册名")
    private String name;

    @ApiModelProperty(value = "相册描述")
    private String description;

    @ApiModelProperty(value = "是否加密 0：否 1：是")
    private Integer isLock;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)

    @JsonFormat(pattern = "yyyy-mm-dd", timezone = "GMT+8")
    private LocalDateTime createTime;


    @TableField(exist = false)
    private Integer photoNum;
}
