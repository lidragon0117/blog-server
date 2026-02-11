package com.lilong.blog.respone.agent;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:33
 * @description
 */
public class ChatCompletionResponse {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatCompletionChoice> choices;
    private Usage usage;

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public long getCreated() {
        return created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setChoices(List<ChatCompletionChoice> choices) {
        this.choices = choices;
    }

    public List<ChatCompletionChoice> getChoices() {
        if (choices == null) {
            return Lists.newArrayList();
        }
        return choices;
    }
}
