package com.lilong.blogaigc.llm;

import com.lilong.blog.enums.agent.LlmModelEnum;
import com.lilong.blog.exception.BusinessException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-11 21:14
 * @description :
 */
@Component
public class LLMServiceFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final Map<String, AbstractLLMChatService> serviceMap = new HashMap<>();

    /**
     * 获取LLM服务
     *
     * @return
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.initLLMChatService();
    }

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void initLLMChatService() {
        serviceMap.put(LlmModelEnum.DEEPSEEK.getModel(), applicationContext.getBean(DeepSeekAIService.class));
        serviceMap.put(LlmModelEnum.KIMI.getModel(), applicationContext.getBean(KimiAIService.class));
        serviceMap.put(LlmModelEnum.OLLAMA.getModel(), applicationContext.getBean(KimiAIService.class));
    }

    /**
     * 获取LLM服务
     *
     * @param model
     * @return
     */
    public static AbstractLLMChatService getLLMService(String model) {
        AbstractLLMChatService llmChatService = serviceMap.get(model);
        if (llmChatService == null) {
            throw new BusinessException("不支持的模型");
        }
        return llmChatService;
    }
}
