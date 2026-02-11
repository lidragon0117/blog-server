package com.lilong.blogaigc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm.config.kimi")
public class KimiLLMPropertiesConfig {

    private String apiKey;

    private String model;
}