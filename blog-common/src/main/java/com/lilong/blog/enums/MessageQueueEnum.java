package com.lilong.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : lilong
 * @date : 2026-02-10 0:25
 * @description :
 */
@Getter
@AllArgsConstructor
public enum MessageQueueEnum {

    QUEUE_LOG_MESSAGE("queue_log_message", "日志消息队列"),

    QUEUE_AI_MESSAGE("queue_ai_persistent_message","AI聊天消息持久化MQ" );
    /**
     * 普通队列
     */
    private String queue;

    private String queueName;
}
