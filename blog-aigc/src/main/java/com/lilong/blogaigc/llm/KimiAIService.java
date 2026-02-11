package com.lilong.blogaigc.llm;

import com.lilong.blog.respone.agent.ChatCompletionMessage;
import com.lilong.blog.respone.agent.ChatCompletionRequest;
import com.lilong.blog.respone.agent.ChatCompletionStreamChoice;
import com.lilong.blogaigc.client.MoonShotAIClient;
import com.lilong.blogaigc.config.properties.KimiLLMPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-11 21:21
 * @description : kimiAI  服务
 */
@Service
public class KimiAIService extends AbstractLLMChatService {

    @Autowired
    private KimiLLMPropertiesConfig kimiLLMPropertiesConfig;

    /**
     * 流式对话
     *
     * @param emitter
     * @param messageList
     * @throws Exception
     */
    public String chat(SseEmitter emitter, List<ChatCompletionMessage> messageList) throws Exception {

        StringBuilder aiMessage = new StringBuilder();

        MoonShotAIClient client = new MoonShotAIClient(kimiLLMPropertiesConfig.getApiKey());


        final List<ChatCompletionMessage> messages = messageList;
        try {
            client.ChatCompletionStream(new ChatCompletionRequest(
                    kimiLLMPropertiesConfig.getModel(),
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
                            if (responseContent != null) {
                                responseContent = responseContent.replaceAll("\n", "<br>");
                                System.out.println(responseContent);
                                emitter.send(responseContent);
                                aiMessage.append(responseContent);
                            }
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
