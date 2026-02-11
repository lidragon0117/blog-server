package com.lilong.blogaigc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm.config.liblib")
public class LibLibPropertiesConfig {

    private String accessKey;

    private String SecretKey;
}