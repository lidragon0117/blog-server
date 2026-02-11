package com.lilong.blogaigc.llm;

import com.lilong.blog.respone.agent.ChatCompletionMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 21:16
 * @description : 抽象LLM智能聊天服务
 */
public abstract class AbstractLLMChatService {
    /**
     * 流式对话
     *
     * @param emitter
     * @param messageList
     * @return
     * @throws Exception
     */
    public abstract String chat(SseEmitter emitter,
                                List<ChatCompletionMessage> messageList) throws Exception;
}
