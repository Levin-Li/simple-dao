package com.levin.commons.dao.support;

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
public class BeanScannerConfigurer
        implements
        BeanFactoryPostProcessor,
        ApplicationContextAware {

    private String[] scanPackages;

    private ApplicationContext applicationContext;

    public void setScanPackages(String... scanPackages) {
        this.scanPackages = scanPackages;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        EntityRepositoryDefinitionScanner scanner = new EntityRepositoryDefinitionScanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(this.applicationContext);
        scanner.scan(scanPackages);
    }
}