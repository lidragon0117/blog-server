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
public enum MessageBroadChannelEnum {

    QUEUE_LOG_MESSAGE_CHANNEL("queue_log_message_channel", "聊天消息队列"),
    ;

    private String channel;

    private String channelDesc;

}
