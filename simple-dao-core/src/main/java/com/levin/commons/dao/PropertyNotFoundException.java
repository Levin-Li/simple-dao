package com.levin.commons.dao;

public class PropertyNotFoundException extends IllegalArgumentException {
    public PropertyNotFoundException() {
        super();
    }

    public PropertyNotFoundException(String s) {
        super(s);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyNotFoundException(Throwable cause) {
        super(cause);
    }
}
