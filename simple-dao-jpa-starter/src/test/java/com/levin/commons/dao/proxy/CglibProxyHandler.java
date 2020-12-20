package com.levin.commons.dao.proxy;

import com.levin.commons.dao.JpaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
public class CglibProxyHandler implements org.springframework.cglib.proxy.MethodInterceptor {


    @Autowired
    JpaDao jpaDao;

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        return " CglibProxyHandler " + getClass() + jpaDao;

    }

}
