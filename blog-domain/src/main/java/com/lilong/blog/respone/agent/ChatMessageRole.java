package com.lilong.blog.respone.agent;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:35
 * @description
 */
public enum ChatMessageRole {

    SYSTEM, USER, ASSISTANT;

    public String value() {
        return this.name().toLowerCase();
    }
}
