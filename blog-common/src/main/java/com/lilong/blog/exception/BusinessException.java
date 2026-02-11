package com.lilong.blog.exception;

/**
 * @author : lilong
 * @date : 2026-02-09 12:33
 * @description :
 */
public class BusinessException extends RuntimeException {

    private Integer code = 500;

    public BusinessException() {
        super();
    }

    public BusinessException(String msg) {
        super(msg);
    }


    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getCode() {
        return code;
    }
}
