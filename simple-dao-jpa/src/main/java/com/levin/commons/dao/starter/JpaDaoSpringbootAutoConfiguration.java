package com.levin.commons.dao.starter;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.simple.RepositoryBeanConfigurer;
import com.levin.commons.dao.support.JpaDaoImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import javax.persistence.EntityManagerFactory;

@Configuration
@Role(BeanDefinition.ROLE_APPLICATION)
public class JpaDaoSpringbootAutoConfiguration {
    //    这些是springboot特有的，常见的条件依赖注解有：
//
//    @ConditionalOnBean，仅在当前上下文中存在某个bean时，才会实例化这个Bean。
//
//    @ConditionalOnClass，某个class位于类路径上，才会实例化这个Bean。
//
//    @ConditionalOnExpression，当表达式为true的时候，才会实例化这个Bean。
//
//    @ConditionalOnMissingBean，仅在当前上下文中不存在某个bean时，才会实例化这个Bean。
//
//    @ConditionalOnMissingClass，某个class在类路径上不存在的时候，才会实例化这个Bean。
//
//    @ConditionalOnNotWebApplication，不是web应用时才会实例化这个Bean。
//
//    @AutoConfigureAfter，在某个bean完成自动配置后实例化这个bean。
//
//    @AutoConfigureBefore，在某个bean完成自动配置前实例化这个bean。


    String[] scanPackages = {};


    @Bean
    @ConditionalOnBean(EntityManagerFactory.class)
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "com.levin.commons.dao", value = "scanPackages")
    @ConfigurationProperties(prefix = "com.levin.commons.dao")
    RepositoryBeanConfigurer newRepositoryBeanConfigurer(String... scanPackages) {
        return new RepositoryBeanConfigurer()
                .setScanPackages(scanPackages);
    }

}
