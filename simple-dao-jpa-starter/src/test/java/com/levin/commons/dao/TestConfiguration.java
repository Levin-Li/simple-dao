package com.levin.commons.dao;

import com.levin.commons.dao.proxy.*;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.service.proxy.EnableProxyBean;
import com.levin.commons.service.proxy.ProxyBeanScan;
import com.levin.commons.service.proxy.ProxyBeanScans;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootConfiguration
@EnableAutoConfiguration

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com .levin .", " .. .", "com.lev", "com.aaa. .. ..", " aa. bb .. . cc ", " . . ", " . "})

@ProxyBeanScans({
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"org .dao .test", " ", ""}),
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"com. levin. commons . dao.."}),

        @ProxyBeanScan(scanType = API.class, invocationHandlerClass = JdkProxyHandler.class
                , basePackages = {"com. levin."}),

        @ProxyBeanScan(scanType = API2.class, invocationHandlerClass = CglibProxyHandler.class
                , basePackages = {"com. levin."}),


        @ProxyBeanScan(scanType = API3.class, invocationHandlerClass = AopProxyHandler.class
                , basePackages = {"com. levin."}),
})

@EnableProxyBean(registerTypes = {API.class, API2.class, API3.class})
//@EnableProxyBean

@EntityScan({"com.levin.commons.dao"})

@ComponentScan("com.levin.commons.dao")
public class TestConfiguration {


}
