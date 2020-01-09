package com.levin.commons.dao.util;

public class WarnException extends RuntimeException {

    public WarnException(String message) {
        super(message);
    }

    public WarnException(String message, Throwable cause) {
        super(message, cause);
    }
}
