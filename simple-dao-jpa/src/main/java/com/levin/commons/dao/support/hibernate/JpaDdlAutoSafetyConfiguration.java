package com.levin.commons.dao.support.hibernate;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Hibernate 执行 DDL 前再次校验最终属性。
 * 早期环境校验后，容器配置和其他 HibernatePropertiesCustomizer 仍可能改变属性，
 * 因此必须在初始化 Hibernate 前拦截危险值，防止绕过早期检查而删除或重建业务表。
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
public class JpaDdlAutoSafetyConfiguration {
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static HibernatePropertiesCustomizer simpleDaoDdlAutoSafetyCustomizer() {
        return HibernateDdlAutoGuard::validateProperties;
    }
}
