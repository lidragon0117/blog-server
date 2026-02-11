package com.lilong.blog.constants;

import lombok.Data;

/**
 * @author : lilong
 * @date : 2025-11-21 10:07
 * @description :通用常量
 */
@Data
public class CommonConstants {

    @Data
    public class HttpConstants {
        /**
         * IP
         */
        public static final String IP = "CURRENT-IP";

        public static final String PING = "ping";

        public final static String HTTP = "http://";
    }

    @Data
    public class MessageQueueConstants {
        public static final String REDIS = "redis";

        public static final String ROCKETMQ = "rocketmq";

        public static final String KAFKA = "kafka";

        public static final String RABBITMQ = "rabbitmq";
    }

    @Data
    public class UserConstants {
        public static final String USER_ID_PREFIX = "U";
        public static final String ROBOT_ID_PREFIX = "R";
        public static final String GROUP_ID_PREFIX = "G";
        public static final String PUBLIC_ACCOUNT_ID_PREFIX = "P";
        public static final String SERVER_ACCOUNT_ID_PREFIX = "S";
    }
}
