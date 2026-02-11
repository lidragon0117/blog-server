package com.lilong.blogaigc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm.config.deepseek")
public class DeepseekLLMPropertiesConfig {

    private String apiKey;
    
    private String model;
}