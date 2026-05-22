package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativePostgreSQLJsonArrayAppendFunctionProbeTest {

    @Test
    void shouldDetectHibernate727RootPathRenderingBug() {
        assertTrue(NativePostgreSQLJsonArrayAppendFunctionProbe.isRootPathRenderingBroken(new TypeConfiguration()));
    }

    @Test
    void shouldNotRequireOverrideWhenRootPathArrayIsWellFormed() {
        assertFalse(NativePostgreSQLJsonArrayAppendFunctionProbe.isRenderingBroken(
                "jsonb_set_lax(t.d,t.p,(t.d)#>t.p||to_jsonb(?),false,'return_target')"
                        + " from (values(doc,array[]::text[])) t(d,p)"
        ));
    }

    @Test
    void simpleDaoJpaShouldImportHibernateAutoConfigurationDirectly() throws Exception {
        var resources = Thread.currentThread().getContextClassLoader().getResources(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        );
        boolean found = false;
        while (resources.hasMoreElements()) {
            try (var reader = new InputStreamReader(resources.nextElement().openStream(), StandardCharsets.UTF_8)) {
                String imports = new java.io.BufferedReader(reader).lines().collect(Collectors.joining("\n"));
                found = found || imports.contains(SimpleDaoHibernateAutoConfiguration.class.getName());
            }
        }
        assertTrue(found, SimpleDaoHibernateAutoConfiguration.class.getName());
    }

    @Test
    void shouldRewriteExplicitNativePostgreSQLDialectWhenWorkaroundIsRequired() {
        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.dialect", PostgreSQLDialect.class.getName());

        SimpleDaoHibernateAutoConfiguration.postgreSQLJsonFunctionHibernatePropertiesCustomizer()
                .customize(hibernateProperties);

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertTrue(SimpleDaoPostgreSQLDialect.class.getName()
                    .equals(hibernateProperties.get("hibernate.dialect")));
        }
    }

    @Test
    void shouldRewriteSpringJpaDatabasePlatformWhenWorkaroundIsRequired() {
        Map<String, Object> hibernateProperties = new HashMap<>();
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.setDatabasePlatform(PostgreSQLDialect.class.getName());

        SimpleDaoHibernateAutoConfiguration.postgreSQLJsonFunctionHibernatePropertiesCustomizer(jpaProperties)
                .customize(hibernateProperties);

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertTrue(SimpleDaoPostgreSQLDialect.class.getName()
                    .equals(hibernateProperties.get("hibernate.dialect")));
        }
    }
}
