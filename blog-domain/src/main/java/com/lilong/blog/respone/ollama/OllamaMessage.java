package com.lilong.blog.respone.ollama;

import lombok.Data;

/**
 * @ClassName OllamaMessage
 * @Description TODO
 * @Author wangjinfei
 * @Date 2025/2/15 20:11
 */
@Data
public class OllamaMessage {
    private String role;
    private String content;
}
