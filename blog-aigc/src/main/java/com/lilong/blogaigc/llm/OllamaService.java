package com.lilong.blogaigc.llm;

import com.lilong.blog.respone.agent.ChatCompletionMessage;
import com.lilong.blog.respone.agent.ChatCompletionRequest;
import com.lilong.blog.respone.agent.ChatCompletionStreamChoice;
import com.lilong.blogaigc.client.OllamaClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 21:21
 * @description :
 */
@Service
@AllArgsConstructor
public class OllamaService extends AbstractLLMChatService {
    @Nullable
    private OllamaClient ollamaClient;

    /**
     * 流式对话
     *
     * @param emitter
     * @param messageList
     * @return String
     * @author wangjinfei
     * @date 2025/2/18 22:57
     */
    @Override
    public String chat(SseEmitter emitter, List<ChatCompletionMessage> messageList)
            throws Exception {
        // 用于记录流式推理完整结果，返回用于，用于对话消息持久化
        StringBuilder aiMessage = new StringBuilder();

        final List<ChatCompletionMessage> messages = messageList;
        try {
            ollamaClient.ChatCompletionStream(new ChatCompletionRequest(
                    ollamaClient.getModel(),
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
                            if (responseContent.equals("```") || responseContent.equals("json")) {
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
            e.printStackTrace();
        }
        return aiMessage.toString();
    }
}
