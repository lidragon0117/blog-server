package com.lilong.blog.helper.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @Author lilong
 * @Date 2024/10/29 23:27
 * @description
 */
@Slf4j
public class SseEmitterHelper {

    /**
     * 维护用户对话的SSE
     */
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, SseEmitter>> sseEmitterMap = new ConcurrentHashMap<>();

    /**
     * 判断当前用户SSE链接是否在当前节点
     *
     *
     * @param biz
     * @param userId
     * @return
     */
    public static boolean isExist(String biz, String userId) {
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(biz);
        return userSseEmitter.get(userId) != null;
    }

    /**
     * 获取用户的 SseEmitter 对象，如果不存在重新创建一个
     *
     * @param userId
     * @return
     */
    public static SseEmitter get(String biz, String userId) {
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(biz);
        if (userSseEmitter == null) {
            userSseEmitter = new ConcurrentHashMap<>();
        }
        SseEmitter sseEmitter = userSseEmitter.get(userId);
        if (sseEmitter == null) {
            sseEmitter = create(biz, userId);
        }
        return sseEmitter;
    }

    /**
     * 删除用户 SseEmitter 对象
     *
     * @param userId
     */
    public static void remove(String biz, String userId) {
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(biz);
        userSseEmitter.remove(userId);
    }

    /**
     * 创建SseEmitter
     *
     * @param userId
     * @return
     */
    private static SseEmitter create(String biz, String userId) {
        SseEmitter sseEmitter = new SseEmitter();
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(biz);
        if (userSseEmitter == null) {
            userSseEmitter = new ConcurrentHashMap<>();
        }
        userSseEmitter.put(userId, sseEmitter);
        sseEmitterMap.put(biz, userSseEmitter);
        sseEmitter.onCompletion(completionCallBack(biz, userId));
        sseEmitter.onError(errorCallBack(biz, userId));
        sseEmitter.onTimeout(timeoutCallBack(biz, userId));
        log.info("SSE Connection created =====> biz={}, userId={}", biz, userId);
        return sseEmitter;
    }

    private static Runnable completionCallBack(String biz, String userId) {
        return () -> {
            log.info("结束连接=====> userId={}", userId);
            remove(biz, userId);
        };
    }
    private static Runnable timeoutCallBack(String biz, String userId){
        return ()->{
            log.info("连接超时=====> userId={}", userId);
            remove(biz, userId);
        };
    }
    private static Consumer<Throwable> errorCallBack(String biz, String userId){
        return throwable -> {
            log.info("连接失败=====> userId={}", userId);
            remove(biz, userId);
        };
    }

    /**
     * sse 消息推送
     *
     * @param biz
     * @param userId
     * @param message
     */
    public static void send(String biz, String userId, String message) {
        try {
            SseEmitter sseEmitter = get(biz, userId);
            sseEmitter.send(message);
            sseEmitter.send("finished");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
