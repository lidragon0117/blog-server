package com.lilong.blog.respone.agent;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:32
 * @description
 */
public class Usage {

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }
}
