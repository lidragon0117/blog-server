package com.lilong.blog.service.message.producer;

import com.lilong.blog.enums.MessageBroadChannelEnum;
import com.lilong.blog.enums.MessageQueueEnum;
import com.lilong.blog.utils.JsonUtil;

/**
 * @author : lilong
 * @date : 2026-02-10 0:26
 * @description :
 */
public abstract class AbstractMessageQueueSender<T, P> implements MessageQueueSender<T, P> {
    /**
     * 序列化消息，默认使用Json序列化方式(如果有特殊情况，可以在具体的实现类中重写该方法)
     *
     * @param message
     * @return
     */
    protected String serializeMessage(T message) {

        if (message instanceof String) {
            return message.toString();
        }
        return JsonUtil.toJsonString(message);
    }

    /**
     * 消息实际发送逻辑
     *
     * @param queue
     * @param message
     * @return
     */
    protected abstract boolean doSend(String queue, T message);

    /**
     * 消息广播
     *
     * @param channel 频道
     * @param message 消息
     * @return
     */
    protected abstract boolean broadSend(String channel, T message);

    /**
     * 优先级队列消息推送
     *
     * @param channel
     * @param message
     * @param priority 精准到毫秒（redis product传未来某个时刻-毫秒，RocketMq传延迟消费时间-单位秒）
     * @return
     */
    protected abstract boolean prioritySend(String channel, T message, P priority);

    /**
     * 消息发送
     *
     * @param queue 消息队列
     * @param message 消息
     */
    @Override
    public boolean send(MessageQueueEnum queue, T message) {
       return this.doSend(queue.getQueue(), message);
    }

    /**
     * 优先级消息推送
     *
     * @param queue    推送队列
     * @param message  消息
     * @param priority 优先级
     * @return
     */
    @Override
    public boolean prioritySend(MessageQueueEnum queue, T message, P priority) {
        return this.prioritySend(queue.getQueue(), message, priority);
    }

    /**
     * 消息发送
     *
     * @param channel 渠道
     * @param message 消息
     */
    @Override
    public void broadSend(MessageBroadChannelEnum channel, T message) {
        this.broadSend(channel.getChannel(), message);
    }
}
