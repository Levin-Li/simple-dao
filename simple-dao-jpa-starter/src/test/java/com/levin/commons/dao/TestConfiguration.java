package com.levin.commons.dao;

import com.levin.commons.conditional.ConditionalOn;
import com.levin.commons.conditional.ConditionalOnList;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.proxy.API;
import com.levin.commons.dao.proxy.APIFactoryBean;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.JpaDaoImpl;
import com.levin.commons.service.proxy.EnableProxyBean;
import com.levin.commons.service.proxy.ProxyBeanScan;
import com.levin.commons.service.proxy.ProxyBeanScans;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootConfiguration
@EnableAutoConfiguration

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com .levin ."," .. .","com.lev","com.aaa. .. .."," aa. bb .. . cc "," . . "," . "})

@ProxyBeanScans({
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"org .dao .test"," ",""}),
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"com. levin. commons . dao.."}),

        @ProxyBeanScan(scanType = API.class, invocationHandlerClass = APIFactoryBean.class
                , basePackages = {"com. levin."}),
})

@EnableProxyBean(registerTypes = API.class)
@EntityScan({"com.levin.commons.dao"})

@ComponentScan("com.levin.commons.dao")
public class TestConfiguration {


}
