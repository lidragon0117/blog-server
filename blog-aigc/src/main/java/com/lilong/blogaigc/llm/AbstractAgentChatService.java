package com.lilong.blogaigc.llm;

import com.lilong.blog.base.AgentChat;
import com.lilong.blog.enums.MessageQueueEnum;
import com.lilong.blog.enums.agent.PromptTemplateEnum;
import com.lilong.blog.respone.agent.FunctionCallResponse;
import com.lilong.blog.service.message.producer.MessageQueueProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-11 19:58
 * @description : 抽象智能聊天服务
 */
public abstract class AbstractAgentChatService<T> {

    @Value("${llm.config.model}")
    private String model;
    @Autowired
    private IntentRecognitionService intentRecognitionService;

    @Autowired
    private FreeMarkEngineService freeMarkEngineService;

    @Autowired
    private MessageQueueProducer<String, String> messageQueueProducer;

    /**
     * 获取模型
     *
     * @return
     */
    public String getModel() {
        return this.model;
    }

    /**
     * 智能聊天
     *
     * @param agentChat
     */
    public abstract void chat(T agentChat);

    /**
     * 意图识别
     *
     * @param vars
     * @param promptTemplate
     * @return
     * @throws Exception
     */
    public FunctionCallResponse getFunction(Map<String, Object> vars, PromptTemplateEnum promptTemplate) throws Exception {
        return null;
    }

    /**
     * 获取模版内容
     *
     * @param vars
     * @param promptTemplate
     * @return
     */
    public String getPrompt(Map<String, Object> vars, PromptTemplateEnum promptTemplate) {
        return freeMarkEngineService.getContentByTemplate(vars, promptTemplate);
    }

    /**
     * 持久化消息
     *
     * @param userId
     * @param messageChat
     */
    protected void persistentAiMessage(String userId, String messageChat) {
        // TODO 消息持久化
        messageQueueProducer.send(MessageQueueEnum.QUEUE_AI_MESSAGE, userId + ":" + messageChat);
    }
}
