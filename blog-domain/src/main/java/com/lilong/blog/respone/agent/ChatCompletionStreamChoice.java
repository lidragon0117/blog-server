package com.lilong.blog.respone.agent;


import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:32
 * @description
 */
public class ChatCompletionStreamChoice {

    private int index;
    private ChatCompletionStreamChoiceDelta delta;

    @SerializedName("finish_reason")
    private String finishReason;
    private Usage usage;

    public int getIndex() {
        return index;
    }

    public ChatCompletionStreamChoiceDelta getDelta() {
        return delta;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDelta(ChatCompletionStreamChoiceDelta delta) {
        this.delta = delta;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public ChatCompletionStreamChoice(int index, ChatCompletionStreamChoiceDelta delta, String finishReason, Usage usage) {
        this.index = index;
        this.delta = delta;
        this.finishReason = finishReason;
        this.usage = usage;
    }
}
