package com.levin.commons.dao;

/**
 * Created by echo on 2017/4/1.
 */
public class DaoSecurityException extends RuntimeException {

    //可以展示给用户看的异常
    String errorInfo;

    public DaoSecurityException(String message) {
        super(message);
    }

    public DaoSecurityException(String message, String errorInfo) {
        super(message);
        this.errorInfo = errorInfo;
    }

    public DaoSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoSecurityException(Throwable cause) {
        super(cause);
    }

    public DaoSecurityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
