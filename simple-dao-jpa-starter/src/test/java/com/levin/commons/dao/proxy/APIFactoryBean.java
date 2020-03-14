package com.levin.commons.dao.proxy;

import com.levin.commons.dao.JpaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class APIFactoryBean implements InvocationHandler {


    @Autowired
    JpaDao jpaDao;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return " test " + jpaDao;

    }

}
