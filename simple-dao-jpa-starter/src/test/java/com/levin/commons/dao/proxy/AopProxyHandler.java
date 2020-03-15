package com.levin.commons.dao.proxy;

import com.levin.commons.dao.JpaDao;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AopProxyHandler implements MethodInterceptor {

    @Autowired
    JpaDao jpaDao;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {


        return " AopProxyHandler " + getClass() + jpaDao;

    }

}
