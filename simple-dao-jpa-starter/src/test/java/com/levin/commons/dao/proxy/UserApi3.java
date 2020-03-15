package com.levin.commons.dao.proxy;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.service.proxy.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManagerFactory;

@API3
public abstract class UserApi3 {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    JpaDao jpaDao;

    public String getName() {

        return "DefaultName";

    }

    public String getEMF() {
        return ProxyFactoryBean.getProxyInvokeResult() + " " + getClass().getName();
    }

    public abstract String getId();


}
