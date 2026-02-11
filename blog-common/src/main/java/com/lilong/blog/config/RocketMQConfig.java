package com.lilong.blog.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : lilong
 * @date : 2026-02-10 1:18
 * @description : RocketMQ 消息队列
 */
@Configuration
public class RocketMQConfig {
    @Value("${rocketmq.producer.group:blog}")
    private String producerGroup;

    @Value("${rocketmq.name-server:192.168.180.103:9876}")
    private String nameServer;

    @Bean
    public RocketMQTemplate rocketMQTemplate() {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        rocketMQTemplate.setProducer(producer);
        return rocketMQTemplate;
    }
}
