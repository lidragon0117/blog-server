package com.lilong.blog.dto.feedback;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: quequnlong
 * @date: 2025/1/12
 * @description:
 */
@Data
public class SysFeedbackQueryDto {

    private String source;


    private Long id;


    private Long userId;

    private String type;

    private String content;


    private String email;


    private String replyContent;


    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
