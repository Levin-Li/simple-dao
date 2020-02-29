package com.levin.commons.dao.repository;

/**
 * 代理方法执行异常
 *
 */
public class ProxyMethodInvokeException
        extends RuntimeException {


    public ProxyMethodInvokeException(String message) {
        super(message);
    }

    public ProxyMethodInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyMethodInvokeException(Throwable cause) {
        super(cause);
    }

}
