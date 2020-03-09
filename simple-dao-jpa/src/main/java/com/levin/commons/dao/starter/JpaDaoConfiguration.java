package com.levin.commons.dao.starter;

import com.levin.commons.conditional.ConditionalOn;
import com.levin.commons.conditional.ConditionalOnList;
import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.JpaDaoImpl;
import com.levin.commons.service.proxy.EnableProxyBean;
import com.levin.commons.service.proxy.ProxyBeanScan;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

//@Role(BeanDefinition.ROLE_SUPPORT)

//@Import(JpaDaoRegistrar.class)

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com.levin.commons.dao.repository"})

@EnableProxyBean(registerTypes = EntityRepository.class)

//@EnableTransactionManagement
//@EnableAspectJAutoProxy
public class JpaDaoConfiguration implements ApplicationContextAware {

    @Bean("com.levin.commons.dao.JpaDao")
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnClass, types = {Eq.class, MiniDao.class, JpaDao.class, JpaDaoImpl.class}),
            @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JpaDao.class),
    })
    FactoryBean newJpaDao() {

        //务必要返回代理对象，否则事务扫描，不会生效

        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();

        JpaDaoImpl target = new JpaDaoImpl();

       // proxyFactoryBean.setProxyTargetClass(true);

//        context.getAutowireCapableBeanFactory().autowireBean(target);
//        target.setApplicationContext(context);

        context.getAutowireCapableBeanFactory()
                .configureBean(target,JpaDao.class.getName());


        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.setSingleton(true);

        return proxyFactoryBean;
    }


    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
