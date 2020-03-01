package com.levin.commons.dao.proxy;

import com.levin.commons.service.proxy.ProxyFactoryBean;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class APIFactoryBean extends ProxyFactoryBean {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return "test";
    }

}
