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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
public class JpaDaoConfiguration implements ApplicationContextAware, BeanFactoryPostProcessor {
    @Bean
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnClass, types = {Eq.class, MiniDao.class, JpaDao.class, JpaDaoImpl.class}),
            @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JpaDao.class),
    })
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }

    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

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
