package com.lilong.blog.respone.agent;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-11 21:23
 * @description :
 */
@Data
public class ChatCompletionMessage {
    @ApiModelProperty(value = "角色")
    public String role;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "内容")
    public String content;
    @ApiModelProperty(value = "部分")
    public Boolean partial;

    public ChatCompletionMessage() {

    }

    public ChatCompletionMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatCompletionMessage(String role, String name, String content, Boolean partial) {
        this.role = role;
        this.name = name;
        this.content = content;
        this.partial = partial;
    }
}
