package com.lilong.blogaigc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@TableName("sys_ai_bot")
@ApiModel(value = "插件管理表对象")
public class SysAiBot implements Serializable {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "自增ID")
    private Long id;

    @ApiModelProperty(value = "插件名称")
    private String name;

    @ApiModelProperty(value = "插件CODE")
    private String code;

    @ApiModelProperty(value = "应用描述")
    private String description;

    @ApiModelProperty(value = "状态")
    private Integer status;
    // rule 和shema是mysql的保留字段，需要添加转义符
    @ApiModelProperty(value = "意图插件Schema定义，json结构，用户插件API参数抽取")
    @TableField("`schema`")
    private String schema;

    @ApiModelProperty(value = "抽参规则要求说明补充")
    @TableField("`rule`")
    private String rule;

    @ApiModelProperty(value = "抽参输出示例")
    private String outputExample;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "版本")
    private Integer version;
}
