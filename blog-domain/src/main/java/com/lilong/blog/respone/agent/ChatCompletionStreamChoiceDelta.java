package com.lilong.blog.respone.agent;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:32
 * @description
 */
public class ChatCompletionStreamChoiceDelta {

    private String content;
    private String role;

    public String getContent() {
        return content;
    }

    public String getRole() {
        return role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ChatCompletionStreamChoiceDelta(String content, String role) {
        this.content = content;
        this.role = role;
    }
}
