package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Hibernate integration hooks that must be active even when applications depend on simple-dao-jpa directly.
 */
@AutoConfiguration
public class SimpleDaoHibernateAutoConfiguration {

    @Bean("com.levin.commons.dao.support.hibernate.PostgreSQLJsonFunctionHibernatePropertiesCustomizer")
    public static HibernatePropertiesCustomizer postgreSQLJsonFunctionHibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            if (!SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
                return;
            }

            Object dialect = hibernateProperties.get("hibernate.dialect");
            if (isPostgreSQLDialect(dialect)) {
                hibernateProperties.put("hibernate.dialect", SimpleDaoPostgreSQLDialect.class.getName());
            }
        };
    }

    static boolean isPostgreSQLDialect(Object dialect) {
        if (dialect instanceof Class<?> dialectClass) {
            return PostgreSQLDialect.class.isAssignableFrom(dialectClass);
        }
        if (dialect == null) {
            return false;
        }

        String dialectName = dialect.toString().trim();
        if (PostgreSQLDialect.class.getName().equals(dialectName)) {
            return true;
        }

        try {
            Class<?> dialectClass = Class.forName(
                    dialectName,
                    false,
                    SimpleDaoHibernateAutoConfiguration.class.getClassLoader()
            );
            return PostgreSQLDialect.class.isAssignableFrom(dialectClass);
        } catch (ClassNotFoundException | LinkageError ex) {
            return false;
        }
    }
}
