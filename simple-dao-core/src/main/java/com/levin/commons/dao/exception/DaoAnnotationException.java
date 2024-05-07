package com.levin.commons.dao.exception;

import org.springframework.dao.DataAccessException;

/**
 * Created by echo on 2017/4/1.
 */
public class DaoAnnotationException extends DataAccessException {

    public DaoAnnotationException(String msg) {
        super(msg);
    }

    public DaoAnnotationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
