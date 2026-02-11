package com.lilong.blog.respone.agent;

import java.util.List;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:33
 * @description
 */
public class ChatCompletionStreamResponse {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatCompletionStreamChoice> choices;

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

    public List<ChatCompletionStreamChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<ChatCompletionStreamChoice> choices) {
        this.choices = choices;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
