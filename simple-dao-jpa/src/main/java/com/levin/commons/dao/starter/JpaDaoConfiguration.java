package com.levin.commons.dao.starter;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.proxy.ProxyBeanScan;
import com.levin.commons.dao.proxy.ProxyBeanScanner;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.JpaDaoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_SUPPORT)

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com.levin.commons.dao.repository"})

@Import(ProxyBeanScanner.Registrar.class)

public class JpaDaoConfiguration implements ApplicationContextAware {

    @Bean
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnClass,
                    types = {Eq.class, MiniDao.class, JpaDao.class, JpaDaoImpl.class}),
//            @ConditionalOn(action = ConditionalOn.Action.OnBean, types = {EntityManagerFactory.class}),
    })
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }


    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = com.levin.commons.dao.JpaDao.class)
    JpaDao newJpaDao2() {
        return new JpaDaoImpl();
    }

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = com.levin.commons.dao.JpaDao.class)
    JpaDao newJpaDao3() {
        return new JpaDaoImpl();
    }


    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

//    @Value("${com.levin.commons.dao.repository.scanPackages:}")
//    String[] scanPackages;
//
//    @PostConstruct
//    public void init() {
//
//        if (scanPackages != null
//                && scanPackages.length > 0) {
//            RepositoryDefinitionScanner scanner = new RepositoryDefinitionScanner((BeanDefinitionRegistry) context);
//            scanner.setResourceLoader(this.context);
//            scanner.scan(scanPackages);
//        }
//
//    }

}
