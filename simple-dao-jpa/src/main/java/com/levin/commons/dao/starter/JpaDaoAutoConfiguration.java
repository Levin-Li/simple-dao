package com.levin.commons.dao.starter;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.simple.RepositoryBeanConfigurer;
import com.levin.commons.dao.support.JpaDaoImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_APPLICATION)
public class JpaDaoAutoConfiguration {

    @Bean
    JpaDao newJpaDao() {
        return new JpaDaoImpl();
    }

    @Bean
    RepositoryBeanConfigurer newRepositoryBeanConfigurer(@Value("${com.levin.commons.dao.scanPackages:}") String... scanPackages) {

        if (scanPackages == null || scanPackages.length < 1) {
            scanPackages = new String[]{com.levin.commons.dao.JpaDao.class.getPackage().getName()};
        }

        return new RepositoryBeanConfigurer()
                .setScanPackages(scanPackages);
    }
}
