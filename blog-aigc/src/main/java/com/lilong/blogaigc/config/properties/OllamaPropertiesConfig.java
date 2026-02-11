package com.lilong.blogaigc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm.config.ollama")
public class OllamaPropertiesConfig {

    private String baseUrl;

    private String model;
}
