package com.lilong.blog.service.message.producer;

import com.lilong.blog.enums.MessageBroadChannelEnum;
import com.lilong.blog.enums.MessageQueueEnum;
import com.lilong.blog.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author : lilong
 * @date : 2026-02-10 0:44
 * @description : 消息消费队列发送者
 */
@Component
public class MessageQueueProducer<T, P> {
    @Autowired
    private RocketMQProducer<T, P> rocketMessageQueueSender;

    @Autowired
    private RedisMQProducer<T, P> redisMessageQueueSender;

    /**
     * 消息队列开关配置，支持：rocketmq、redis、kafka等
     */
    @Value("${message-queue.used:rocketmq}")
    private String messageQueueUsed;


    /**
     * 普通队列消息发送
     *
     * @param queue
     * @param message
     */
    public boolean send(MessageQueueEnum queue, T message) {
        return this.getSender().send(queue, message);
    }

    /**
     * 优先级队列消息发送
     *
     * @param queue
     * @param message
     * @param priority
     */
    public boolean prioritySend(MessageQueueEnum queue, T message, P priority) {
        return this.getSender().prioritySend(queue, message, priority);
    }

    /**
     * 广播消息发送
     *
     * @param channel
     * @param message
     */
    public void broadSend(MessageBroadChannelEnum channel, T message) {
        this.getSender().broadSend(channel, message);
    }

    private AbstractMessageQueueSender<T, P> getSender() {
        if (messageQueueUsed.equals("rocketmq")) {
            return rocketMessageQueueSender;
        } else if (messageQueueUsed.equals("redis")) {
            return redisMessageQueueSender;
        }
        throw new BusinessException("不支持的队列类型！");
    }
}
