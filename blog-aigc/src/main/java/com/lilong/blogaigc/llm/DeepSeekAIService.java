package com.lilong.blogaigc.llm;

import com.lilong.blog.respone.agent.ChatCompletionMessage;
import com.lilong.blog.respone.agent.ChatCompletionRequest;
import com.lilong.blog.respone.agent.ChatCompletionStreamChoice;
import com.lilong.blogaigc.client.DeepSeekAIClient;
import com.lilong.blogaigc.config.properties.DeepseekLLMPropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 21:20
 * @description : DeepSeekAi 服务
 */
@Slf4j
@Service
public class DeepSeekAIService extends AbstractLLMChatService {
    @Autowired
    private DeepseekLLMPropertiesConfig deepseekLLMPropertiesConfig;

    /**
     * 聊天
     *
     * @param emitter     sseEmitter
     * @param messageList 消息列表
     * @return 响应
     * @throws Exception 异常
     */
    /**
     * 流式对话
     *
     * @param emitter
     * @param messageList
     * @throws Exception
     */
    public String chat(SseEmitter emitter, List<ChatCompletionMessage> messageList) throws Exception {

        // 用于记录流式推理完整结果，返回用于，用于对话消息持久化
        StringBuilder aiMessage = new StringBuilder();

        DeepSeekAIClient client = new DeepSeekAIClient(deepseekLLMPropertiesConfig.getApiKey());
        final List<ChatCompletionMessage> messages = messageList;
        try {
            client.ChatCompletionStream(new ChatCompletionRequest(
                    deepseekLLMPropertiesConfig.getModel(),
                    messages,
                    2000,
                    0.3f,
                    1
            )).subscribe(
                    streamResponse -> {
                        if (streamResponse.getChoices().isEmpty()) {
                            return;
                        }
                        for (ChatCompletionStreamChoice choice : streamResponse.getChoices()) {
                            String finishReason = choice.getFinishReason();
                            if (finishReason != null) {
                                emitter.send("finished");
                                continue;
                            }
                            String responseContent = choice.getDelta().getContent();
                            if (responseContent.equals("```") || responseContent.equals("json") ) {
                                continue;
                            }
                            responseContent = responseContent.replaceAll("\n", "<br>");
                            System.out.println(responseContent);
                            emitter.send(responseContent);
                            aiMessage.append(responseContent);
                        }
                    },
                    error -> {
                        error.printStackTrace();
                    },
                    () -> {
//                        emitter.complete(); // 完成事件流发送
                    }
            );
        } catch (Exception e) {
            log.info("LLM 错误：{}", e.getMessage());
        }
        return aiMessage.toString();
    }
}
