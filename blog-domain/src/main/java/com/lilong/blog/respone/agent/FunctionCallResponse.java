package com.lilong.blog.respone.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-11 20:20
 * @description : 意图识别结果返回
 */
@Data
@ApiModel("意图识别结果返回")
public class FunctionCallResponse {
    /**
     * 意图
     */
    @ApiModelProperty("意图")
    private String function;

    /**
     * ai对用户输入query的优化结果
     */
    @ApiModelProperty("ai对用户输入query的优化结果")
    private String prompt;
}
