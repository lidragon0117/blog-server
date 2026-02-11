package com.lilong.blog.constants.agent;

import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-11 19:46
 * @description : 智能体常量
 */
@Data
public class AgentConstants {

    @Data
    public class AiBotConstants {
        public  static final String AGENT_BOT_CODE = "AI_BOT_CHAT";

        public static final String AGENT_BOT_NAME = "AGENT_BOT_NAME";

        public static final String AGENT_BOT_DESCRIPTION = "AGENT_BOT_DESCRIPTION";
    }
    /**
     * AI用户常量
     */
    @Data
    public class AiUserConstants{

        public static final String SYSTEM = "system";

        public static final String USER = "user";

        private static final String ASSISTANT = "assistant";
    }
    /**
     * AI服务常量
     */
    @Data
    public class AiServiceConstants {
        public static final String AI_AGENT_SERVICE = "AI_AGENT_SERVICE";
    }

}
