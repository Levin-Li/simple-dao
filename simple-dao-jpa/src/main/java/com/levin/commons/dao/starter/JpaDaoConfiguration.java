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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration

@Role(BeanDefinition.ROLE_SUPPORT)


@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com.levin.commons.dao.repository"})

@EnableProxyBean(registerTypes = EntityRepository.class)

@Slf4j
public class JpaDaoConfiguration implements ApplicationContextAware {

    /**
     * 因为在注册期 JpaDao bean 已经被引用，所以事务注解不会尝试重试初始化 JpaDao bean
     *
     *
     * <p>
     * 这将导致事务无效，使用代理FactoryBean的方式，可以让@Transactional事务自己加在已有代理对象上面
     *
     * @return
     */
    @Bean("com.levin.commons.dao.JpaDao")
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnClass, types = {Eq.class, MiniDao.class, JpaDao.class, JpaDaoImpl.class}),
            @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JpaDao.class),
    })
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }

/*
    FactoryBean<JpaDao> newJpaDao() {

        //一定要返回 FactoryBean<JpaDao>

        //务必要返回代理对象，否则事务扫描，不会生效

        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();

        JpaDaoImpl target = new JpaDaoImpl();

        // proxyFactoryBean.setProxyTargetClass(true);

//        context.getAutowireCapableBeanFactory().autowireBean(target);
//        target.setApplicationContext(context);


        context.getAutowireCapableBeanFactory()
                .configureBean(target, JpaDao.class.getName());

        try {
            proxyFactoryBean.setProxyInterfaces(new Class[]{JpaDao.class});
        } catch (ClassNotFoundException e) {
        }

        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.setSingleton(true);

        return (FactoryBean) proxyFactoryBean;
    }*/


    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
