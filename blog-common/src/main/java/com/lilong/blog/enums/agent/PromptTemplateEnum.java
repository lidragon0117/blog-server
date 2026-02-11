package com.lilong.blog.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : lilong
 * @date : 2026-02-11 20:24
 * @description :
 */
@Getter
@AllArgsConstructor
public enum PromptTemplateEnum {

    ROBOT_FC("/ftl/ROBOT_FC.ftl", "大模型意图识别"),

    AI_BOT_PARAMETER("/ftl/AI_BOT_PARAMETER.ftl", "大模型抽参"),

    AIBOT_FC("/ftl/AI_BOT_FC.ftl", "我的Ai助手用户输入query意图识别"),

    AIBOT_RAG("/ftl/AI_BOT_RAG.ftl", "我的Ai助手RAG问答");
    private String path;
    private String desc;
}
