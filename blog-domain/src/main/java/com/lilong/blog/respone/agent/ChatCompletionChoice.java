package com.lilong.blog.respone.agent;


import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:33
 * @description
 */
public class ChatCompletionChoice {

    private int index;
    private ChatCompletionMessage message;

    @SerializedName("finish_reason")
    private String finishReason;

    public int getIndex() {
        return index;
    }

    public ChatCompletionMessage getMessage() {
        return message;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMessage(ChatCompletionMessage message) {
        this.message = message;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}
