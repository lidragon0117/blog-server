package com.lilong.blog.respone.agent;

import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:34
 * @description
 */
public class ChatCompletionRequest {

    public String model;
    public List<ChatCompletionMessage> messages;

    @SerializedName("max_tokens")
    public int maxTokens;

    @SerializedName("temperature")
    public float temperature;
    public float topP;

    public Integer n;
    public boolean stream;
    public List<String> stop;

    @SerializedName("presence_penalty")
    public float presencePenalty;

    @SerializedName("frequency_penalty")
    public float frequencyPenalty;

    public String user;

    public List<ChatCompletionMessage> getMessages() {
        return messages;
    }

    public ChatCompletionRequest(String model, List<ChatCompletionMessage> messages, int maxTokens, float temperature, int n) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.n = n;
    }
}
