package com.lilong.blog.service.message.producer;

import com.lilong.blog.enums.MessageBroadChannelEnum;
import com.lilong.blog.enums.MessageQueueEnum;

/**
 * @author : lilong
 * @date : 2026-02-10 0:24
 * @description : T 代表消息 ,P 代表优先级
 */
public interface MessageQueueSender<T, P> {
    /**
     * 普通队列消息发送
     *
     * @param queue
     * @param message
     */
    boolean send(MessageQueueEnum queue, T message);

    /**
     * 优先级队列消息发送
     *
     * @param queue
     * @param message
     * @param priority
     */
    boolean prioritySend(MessageQueueEnum queue, T message, P priority);

    /**
     * 广播消息发送
     *
     * @param channel
     * @param message
     */
    void broadSend(MessageBroadChannelEnum channel, T message);
}
