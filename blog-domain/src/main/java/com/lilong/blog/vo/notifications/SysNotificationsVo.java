package com.lilong.blog.vo.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author : lilong
 * @date : 2026-02-10 19:22
 * @description :
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysNotificationsVo {

    @ApiModelProperty(value = "通知的唯一标识，自增主键")
    private Long id;

    @ApiModelProperty(value = "推送用户id")
    private Long userId;

    @ApiModelProperty(value = "来自用户id")
    private Long fromUserId;

    @ApiModelProperty(value = "文章id")
    private Long articleId;

    @ApiModelProperty(value = "通知的类型，如 system、comment、like 等")
    private String type;

    @ApiModelProperty(value = "通知的标题")
    private String title;

    @ApiModelProperty(value = "通知的具体内容")
    private String message;

    @ApiModelProperty(value = "标记通知是否已读，0 表示未读，1 表示已读")
    private Integer isRead;

    @ApiModelProperty(value = "通知关联的链接，可为空")
    private String link;

    @ApiModelProperty(value = "通知的创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
