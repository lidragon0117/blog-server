package com.lilong.blog.vo.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : lilong
 * @date : 2026-02-10 18:03
 * @description :
 */
@Data
public class SysNoticeVo {

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "公告内容")
    private String content;

    @ApiModelProperty(value = "是否展示")
    private Integer isShow;

    @ApiModelProperty(value = "显示位置 （top：顶部，right:右侧）")
    private String position;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
