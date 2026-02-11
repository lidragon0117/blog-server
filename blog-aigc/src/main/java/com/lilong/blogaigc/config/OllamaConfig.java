package com.lilong.blogaigc.config;

import com.lilong.blogaigc.client.OllamaClient;
import com.lilong.blogaigc.config.properties.OllamaPropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName OllamaConfig
 * @Description TODO
 * @Author wangjinfei
 * @Date 2025/2/18 22:04
 */
@Slf4j
@Component
public class OllamaConfig {
    @Autowired
    private OllamaPropertiesConfig ollamaPropertiesConfig;

    @Bean
    @ConditionalOnProperty(prefix = "llm.config", name = "model", havingValue = "ollama", matchIfMissing = false)
    public OllamaClient ollamaClient(){
        String baseUrl = ollamaPropertiesConfig.getBaseUrl();
        String model = ollamaPropertiesConfig.getModel();

        OllamaClient ollamaClient = new OllamaClient(baseUrl, model);
        log.info("------->>>> ollamaJava客户端注入成功，baseUrl:{}，model:{}", baseUrl, model);
        return ollamaClient;
    }
}
