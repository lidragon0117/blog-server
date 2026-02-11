package com.lilong.blog.enums.agent;

import lombok.Getter;

/**
 * @author : lilong
 * @date : 2026-02-11 21:18
 * @description : 模型枚举
 */
@Getter
public enum LlmModelEnum {

    KIMI("kimi"),

    DEEPSEEK("deepseek"),

    OLLAMA("ollama");

    private String model;

    LlmModelEnum(String model) {
        this.model = model;
    }
}
