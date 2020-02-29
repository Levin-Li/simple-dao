package com.levin.commons.dao.springboot.starter;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.repository.RepositoryDefinitionScanner;
import com.levin.commons.dao.starter.ConditionalOn;
import com.levin.commons.dao.support.JpaDaoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import javax.annotation.PostConstruct;

@Configuration
@Role(BeanDefinition.ROLE_APPLICATION)
public class JpaDaoConfiguration implements ApplicationContextAware {

    public static final String KEY = "com.levin.commons.dao.repository.scanPackages";

    @Bean
    @ConditionalOn(type = ConditionalOn.Type.OnMissingBean, value = "com.levin.commons.dao.JpaDao")
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }


    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Value("${com.levin.commons.dao.repository.scanPackages:}")
    String[] scanPackages;

    @PostConstruct
    public void init() {

        if (scanPackages != null
                && scanPackages.length > 0) {
            RepositoryDefinitionScanner scanner = new RepositoryDefinitionScanner((BeanDefinitionRegistry) context);
            scanner.setResourceLoader(this.context);
            scanner.scan(scanPackages);
        }

    }

}
