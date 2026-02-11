package com.lilong.blogaigc.service.impl;

import com.lilong.blog.constants.agent.AgentConstants;
import com.lilong.blog.enums.agent.PromptTemplateEnum;
import com.lilong.blog.helper.SseEmitterHelper;
import com.lilong.blog.respone.agent.ChatCompletionMessage;
import com.lilong.blog.vo.agent.AiAgentChatVo;
import com.lilong.blogaigc.llm.AbstractAgentChatService;
import com.lilong.blogaigc.llm.AbstractLLMChatService;
import com.lilong.blogaigc.llm.LLMServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * @author : lilong
 * @date : 2026-02-11 19:38
 * @description : AI智能问答服务
 */
@Slf4j
@Service
public class AiBotChatService extends AbstractAgentChatService<AiAgentChatVo> {
    /**
     * AI智能问答
     *
     * @param agentChat
     */
    @Override
    public void chat(AiAgentChatVo agentChat) {
        // 获取连接
        SseEmitter sseEmitter = SseEmitterHelper.get(agentChat.getBizCode(), agentChat.getUserId());
        if (sseEmitter == null) {
            // 链接对象为空，说明当前用户sse链接不在当前节点，直接return，由集群其他节点处理任务
            return;
        }
        // 持久化消息
        this.persistentAiMessage(agentChat.getUserId(), agentChat.getContent());
        // 实现自定义检索：基于文章相似度实现搜索
        //List<ArticleMilvusSearchResponse> searchResponses = this.search(senderId, query);
        Map<String, Object> vars = new HashMap<>();
        vars.put("searchResponses", new ArrayList<>());
        vars.put("input", agentChat.getContent());
        String prompt = super.getPrompt(vars, PromptTemplateEnum.AIBOT_RAG);

        List<ChatCompletionMessage> messageList = Arrays.asList(
                new ChatCompletionMessage(AgentConstants.AiUserConstants.USER, prompt));
        // 调用大模型处理
        try {
            AbstractLLMChatService llmService = LLMServiceFactory.getLLMService(super.getModel());
            String messageChat = llmService.chat(sseEmitter, messageList);
            // 持久化消息
            this.persistentAiMessage(agentChat.getUserId(), messageChat);
        } catch (Exception e) {
            log.error("LLM 错误：{}", e.getMessage());
        }
    }
}
