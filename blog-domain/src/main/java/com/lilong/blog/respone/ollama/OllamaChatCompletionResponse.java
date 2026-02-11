package com.lilong.blog.respone.ollama;

import lombok.Data;

/**
 * @ClassName OllamaChatCompletionResponse
 * @Description TODO
 * @Author wangjinfei
 * @Date 2025/2/15 20:09
 */
@Data
public class OllamaChatCompletionResponse {
    private String model;
    private String createdAt;
    private OllamaMessage message;
    private Boolean done;
    private String totalDuration;
    private String loadDuration;
    private String promptEvalCount;
    private String promptEvalDuration;
    private String evalCount;
    private String evalDuration;
}
