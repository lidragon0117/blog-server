package com.lilong.blog.service.message.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : lilong
 * @date : 2026-02-10 0:30
 * @description :
 */
@Slf4j
@Component
public class RocketMQProducer<T, P> extends AbstractMessageQueueSender<T, P> {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    protected boolean doSend(String queue, T message) {
        String messageStr = super.serializeMessage(message);
        SendResult result = rocketMQTemplate.syncSend(queue, messageStr);
        log.info("RocketMQ Send Message ===> result{}, msg:{}", result, messageStr);
        return SendStatus.SEND_OK.equals(result.getSendStatus());
    }

    @Override
    protected boolean broadSend(String channel, T message) {
        // 对RocketMQ而言，广播模式的关键在于消费者端的配置。消费者需要明确指定消费模式为广播模式
        return this.doSend(channel, message);
    }

    /**
     * 优先级发送
     *
     * @param channel
     * @param message
     * @param priority
     * @return
     */
    @Override
    protected boolean prioritySend(String channel, T message, P priority) {
        String messageJson = super.serializeMessage(message);
        long delayTime = Long.valueOf(String.valueOf(priority));
        SendResult sendResult = rocketMQTemplate.syncSendDelayTimeSeconds(channel, messageJson, delayTime);
        return sendResult.getSendStatus() == SendStatus.SEND_OK;
    }
}
