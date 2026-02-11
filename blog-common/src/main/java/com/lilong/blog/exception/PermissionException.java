package com.lilong.blog.exception;

/**
 * @author : lilong
 * @date : 2026-02-10 1:00
 * @description : 权限异常
 */
public class PermissionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
