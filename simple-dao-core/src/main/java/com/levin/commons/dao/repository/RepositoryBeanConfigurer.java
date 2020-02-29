package com.levin.commons.dao.repository;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 *
 *
 *
 */
public class RepositoryBeanConfigurer
        implements
        BeanFactoryPostProcessor,
        ApplicationContextAware {

    private String[] scanPackages;

    private ApplicationContext applicationContext;

    public RepositoryBeanConfigurer setScanPackages(String... scanPackages) {
        this.scanPackages = scanPackages;
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        RepositoryDefinitionScanner scanner = new RepositoryDefinitionScanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(this.applicationContext);

        scanner.scan(scanPackages);
    }
}