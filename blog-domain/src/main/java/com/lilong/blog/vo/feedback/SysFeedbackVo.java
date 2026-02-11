package com.lilong.blog.vo.feedback;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: lilong
 * @date: 2025/1/12
 * @description:
 */
@Data
@ApiModel(value = "反馈对象vo")
public class SysFeedbackVo implements Serializable {

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "反馈人id")
    private Long userId;

    @ApiModelProperty(value = "反馈类型")
    private String type;

    @ApiModelProperty(value = "反馈内容")
    private String content;

    @ApiModelProperty(value = "联系邮箱")
    private String email;

    @ApiModelProperty(value = "回复内容")
    private String replyContent;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
