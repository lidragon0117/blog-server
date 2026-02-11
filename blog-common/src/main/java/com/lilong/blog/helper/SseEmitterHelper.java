package com.lilong.blog.helper;

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
    public static SseEmitter get(String bizCode, String userId) {
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(bizCode);
        if (userSseEmitter == null) {
            userSseEmitter = new ConcurrentHashMap<>();
        }
        SseEmitter sseEmitter = userSseEmitter.get(userId);
        if (sseEmitter == null) {
            sseEmitter = create(bizCode, userId);
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
    private static SseEmitter create(String bizCode, String userId) {
        // 设置30分钟超时时间，避免SSE连接过早断开
        SseEmitter sseEmitter = new SseEmitter();
        ConcurrentHashMap<String, SseEmitter> userSseEmitter = sseEmitterMap.get(bizCode);
        if (userSseEmitter == null) {
            userSseEmitter = new ConcurrentHashMap<>();
        }
        userSseEmitter.put(userId, sseEmitter);
        sseEmitterMap.put(bizCode, userSseEmitter);
        sseEmitter.onCompletion(completionCallBack(bizCode, userId));
        sseEmitter.onError(errorCallBack(bizCode, userId));
        sseEmitter.onTimeout(timeoutCallBack(bizCode, userId));

        // 立即发送一个连接成功消息，避免客户端长时间等待
        try {
            // 发送命名事件，data 为欢迎消息
            sseEmitter.send(SseEmitter.event()
                    .name("connected")
                    .data("您好！我是智能问答助手，有什么可以帮助您的吗？")
                    .build());
            log.info("发送连接确认消息=====> biz={}, userId={}", bizCode, userId);
        } catch (IOException e) {
            log.error("发送连接确认消息失败=====> userId={}", userId, e);
            sseEmitter.completeWithError(e);
        }
        log.info("SSE Connection created =====> biz={}, userId={}", bizCode, userId);
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
            // 发送命名事件 "message"
            sseEmitter.send(SseEmitter.event()
                    .name("message")
                    .data(message)
                    .build());
            log.info("SSE消息推送成功 =====> biz={}, userId={}, message={}", biz, userId, message);
        } catch (IOException ex) {
            log.error("SSE消息推送失败 =====> biz={}, userId={}", biz, userId, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * sse 消息推送完成
     *
     * @param biz
     * @param userId
     */
    public static void complete(String biz, String userId) {
        try {
            SseEmitter sseEmitter = get(biz, userId);
            // 发送完成事件
            sseEmitter.send(SseEmitter.event()
                    .name("finished")
                    .data("")
                    .build());
            sseEmitter.complete();
            log.info("SSE连接完成 =====> biz={}, userId={}", biz, userId);
        } catch (IOException ex) {
            log.error("SSE连接完成失败 =====> biz={}, userId={}", biz, userId, ex);
        }
    }
}
