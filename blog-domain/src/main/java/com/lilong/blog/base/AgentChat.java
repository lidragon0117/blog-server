package com.lilong.blog.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-11 20:00
 * @description :
 */
@Data
public class AgentChat {
    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private String userId;
    /**
     * 业务编码
     */
    @ApiModelProperty("业务编码")
    private String bizCode;
    /**
     * 聊天内容
     */
    @ApiModelProperty("聊天内容")
    private String content;
}
