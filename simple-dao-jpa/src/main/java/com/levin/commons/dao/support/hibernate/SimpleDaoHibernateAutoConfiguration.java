package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.vendor.Database;

/**
 * Hibernate integration hooks that must be active even when applications depend on simple-dao-jpa directly.
 */
@AutoConfiguration
public class SimpleDaoHibernateAutoConfiguration {

    @Bean("com.levin.commons.dao.support.hibernate.PostgreSQLJsonFunctionHibernatePropertiesCustomizer")
    public static HibernatePropertiesCustomizer postgreSQLJsonFunctionHibernatePropertiesCustomizer(
            ObjectProvider<JpaProperties> jpaPropertiesProvider) {
        return postgreSQLJsonFunctionHibernatePropertiesCustomizer(jpaPropertiesProvider.getIfAvailable());
    }

    public static HibernatePropertiesCustomizer postgreSQLJsonFunctionHibernatePropertiesCustomizer() {
        return postgreSQLJsonFunctionHibernatePropertiesCustomizer((JpaProperties) null);
    }

    static HibernatePropertiesCustomizer postgreSQLJsonFunctionHibernatePropertiesCustomizer(JpaProperties jpaProperties) {
        return hibernateProperties -> {
            if (!SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
                return;
            }

            Object dialect = hibernateProperties.get("hibernate.dialect");
            if (isPostgreSQLDialect(dialect)) {
                hibernateProperties.put("hibernate.dialect", SimpleDaoPostgreSQLDialect.class.getName());
            } else if (dialect == null && isPostgreSQLDatabasePlatform(jpaProperties)) {
                hibernateProperties.put("hibernate.dialect", SimpleDaoPostgreSQLDialect.class.getName());
            }
        };
    }

    private static boolean isPostgreSQLDatabasePlatform(JpaProperties jpaProperties) {
        return jpaProperties != null
                && (isPostgreSQLDialect(jpaProperties.getDatabasePlatform())
                || Database.POSTGRESQL == jpaProperties.getDatabase());
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
