package com.levin.commons.dao.exception;

import org.springframework.dao.DataAccessException;

/**
 * Created by echo on 2017/4/1.
 */
public class DaoSecurityException extends DataAccessException {

    public DaoSecurityException(String msg) {
        super(msg);
    }

    public DaoSecurityException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
