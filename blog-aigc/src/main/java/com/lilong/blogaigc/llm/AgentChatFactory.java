package com.lilong.blogaigc.llm;

import com.lilong.blog.constants.agent.AgentConstants;
import com.lilong.blog.exception.BusinessException;
import com.lilong.blogaigc.service.impl.AiBotChatService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : lilong
 * @date : 2026-02-11 22:06
 * @description :
 */
@Component
public class AgentChatFactory implements InitializingBean, ApplicationContextAware {


    private ApplicationContext applicationContext;
    private static final Map<String, AbstractAgentChatService> serviceMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initServiceMap();
    }

    private void initServiceMap() {
        serviceMap.put(AgentConstants.AiServiceConstants.AI_AGENT_SERVICE, applicationContext.getBean(AiBotChatService.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static AbstractAgentChatService getService(String serviceName) {

        AbstractAgentChatService abstractAgentChatService = serviceMap.get(serviceName);
        if (abstractAgentChatService == null) {
            throw new BusinessException("不支持该服务");
        }
        return abstractAgentChatService;
    }
}
