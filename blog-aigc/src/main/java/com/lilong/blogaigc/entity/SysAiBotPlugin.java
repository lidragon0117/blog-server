package com.lilong.blogaigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : lilong
 * @date : 2026-02-11 17:02
 * @description :
 */
@Data
@TableName("sys_ai_bot_plugin")
@ApiModel(value = "插件API管理表对象")
public class SysAiBotPlugin implements Serializable {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "自增ID")
    private Long id;

    @ApiModelProperty(value = "BOT code")
    private String botCode;

    @ApiModelProperty(value = "应用名称 ")
    private String api;

    @ApiModelProperty(value = "请求方式：GET、POST")
    private String method;
}