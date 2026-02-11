package com.lilong.tools.consumer;

import cn.hutool.json.JSONUtil;
import com.lilong.tools.entity.SysOperateLog;
import com.lilong.tools.service.SysOperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : lilong
 * @date : 2026-02-10 1:13
 * @description : 持久化RocketMQ队列
 */
@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = "blog_tool_group",
        topic = "queue_log_message"
)
public class OperateLogRocketMQConsumer implements RocketMQListener<String> {

    @Autowired
    private SysOperateLogService operateLogService;

    @Override
    public void onMessage(String message) {
        log.info("操作日志接收mq==========>:{}", message);
        try {
            SysOperateLog sysOperateLog = JSONUtil.toBean(message, SysOperateLog.class);
            operateLogService.save(sysOperateLog);
        } catch (Exception e) {
            log.error("操作日志保存失败:message{},exception:{}", message, e);
        }
    }
}
