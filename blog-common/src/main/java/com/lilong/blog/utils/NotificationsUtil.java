//package com.lilong.blog.utils;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
///**
// * @author: lilong
// * @date: 2025/3/24
// * @description:
// */
//@Component
//@RequiredArgsConstructor
//public class NotificationsUtil {
//
//    private final SysArticleMapper sysArticleMapper;
//
//    private final SysNotificationsMapper baseMapper;
//
//
//    public void publish(SysNotifications sysNotifications) {
//        SysArticle sysArticle = new SysArticle();
//        switch (sysNotifications.getType()) {
//            case "comment":
//                if (sysNotifications.getUserId() == null) {
//                    sysArticle = sysArticleMapper.selectById(sysNotifications.getArticleId());
//                    sysNotifications.setUserId(sysArticle.getUserId());
//                }
//                break;
//            case "like":
//                if (sysNotifications.getArticleId() != null) {
//                    sysArticle = sysArticleMapper.selectById(sysNotifications.getArticleId());
//                    sysNotifications.setUserId(sysArticle.getUserId());
//                }
//                sysNotifications.setMessage("点赞了文章：" + sysArticle.getTitle());
//                break;
//            case "unLike":
//                break;
//            default:
//                break;
//        }
//        baseMapper.insert(sysNotifications);
//    }
//}
