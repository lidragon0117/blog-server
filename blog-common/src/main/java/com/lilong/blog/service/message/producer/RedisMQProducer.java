package com.lilong.blog.service.message.producer;

import com.lilong.blog.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : lilong
 * @date : 2026-02-10 0:34
 * @description :
 */
@Slf4j
@Component
public class RedisMQProducer<T, P> extends AbstractMessageQueueSender<T, P> {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected boolean doSend(String queue, T message) {
        String messageValue = super.serializeMessage(message);
        redisUtil.lleftPush(queue, messageValue);
        return true;
    }

    @Override
    protected boolean broadSend(String channel, T message) {
        String messageValue = super.serializeMessage(message);
        redisUtil.convertAndSend(channel, messageValue);
        return true;
    }

    @Override
    protected boolean prioritySend(String channel, T message, P priority) {
        String messageValue = super.serializeMessage(message);
        return redisUtil.zAdd(channel, messageValue, Long.valueOf(String.valueOf(priority)), -1L);
    }
}
