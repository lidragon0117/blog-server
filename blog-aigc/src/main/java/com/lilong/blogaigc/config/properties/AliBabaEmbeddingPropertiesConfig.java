package com.lilong.blogaigc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm.embedding.alibaba")
public class AliBabaEmbeddingPropertiesConfig {

    private String apiKey;
}