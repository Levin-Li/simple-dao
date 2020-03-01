package com.levin.commons.dao;

import com.levin.commons.dao.proxy.ProxyBeanScan;
import com.levin.commons.dao.proxy.ProxyBeanScans;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootConfiguration
@EnableAutoConfiguration

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com .levin ."," .. .","com.lev","com.aaa. .. .."," aa. bb .. . cc "," . . "," . "})

@ProxyBeanScans({
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"org .dao .test"," ",""}),
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"com. levin. commons . dao.."})
})
public class TestConfiguration {


}
