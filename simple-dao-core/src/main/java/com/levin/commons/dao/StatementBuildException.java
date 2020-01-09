package com.levin.commons.dao;

/**
 * Created by echo on 2017/4/1.
 */
public class StatementBuildException extends RuntimeException {

    //可以展示给用户看的异常
    String errorInfo;

    public StatementBuildException(String message) {
        super(message);
    }

    public StatementBuildException(String message, String errorInfo) {
        super(message);
        this.errorInfo = errorInfo;
    }

    public StatementBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatementBuildException(Throwable cause) {
        super(cause);
    }

    public StatementBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
