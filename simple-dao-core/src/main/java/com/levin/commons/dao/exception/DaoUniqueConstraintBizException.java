package com.levin.commons.dao.exception;

import org.springframework.dao.DataAccessException;


/**
 * 业务异常：唯一约束
 */
public class DaoUniqueConstraintBizException extends DataAccessException {

    public DaoUniqueConstraintBizException(String msg) {
        super(msg);
    }

    public DaoUniqueConstraintBizException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
