package com.levin.commons.dao.ddlsafety;

import com.levin.commons.dao.support.hibernate.JpaDdlAutoSafetyConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class HibernateDdlAutoSafetyTest {
    @TempDir Path dir;
    private static final AtomicInteger CREATED = new AtomicInteger();

    @Test
    void unsafeYamlMustFailBeforeAnySpringBeanIsCreated() throws Exception {
        for (String action : new String[]{"create", "create-drop", "drop", "validate", "CrEaTe", "DROP", "VaLiDaTe", "  CREATE  ", "invalid", "", "   "}) {
            CREATED.set(0);
            Files.writeString(dir.resolve("application.yml"), "spring:\n  jpa:\n    hibernate:\n      ddl-auto: '" + action + "'\n");
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> app(MarkerConfiguration.class).run(location()));
            assertTrue(messages(error).contains("只允许 none 或 update"), messages(error));
            assertEquals(0, CREATED.get(), "必须在创建 Bean 之前拒绝配置");
        }
    }

    @Test
    void openInViewMustFailBeforeBeansAndRespectSafeOverride() throws Exception {
        for (String value : new String[]{"true", "TrUe", "invalid", ""}) {
            CREATED.set(0);
            Files.writeString(dir.resolve("application.yml"), "spring:\n  jpa:\n    open-in-view: '" + value + "'\n");
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> app(MarkerConfiguration.class).run(location()));
            assertTrue(messages(error).contains("spring.jpa.open-in-view"));
            assertEquals(0, CREATED.get());
        }
        Files.writeString(dir.resolve("application.yml"), "spring:\n  profiles:\n    active: bad\n  jpa:\n    open-in-view: false\n");
        Files.writeString(dir.resolve("application-bad.yml"), "spring:\n  jpa:\n    open-in-view: true\n");
        CREATED.set(0);
        assertThrows(RuntimeException.class, () -> app(MarkerConfiguration.class).run(location()));
        assertEquals(0, CREATED.get());
        try (var context = app(MarkerConfiguration.class).run(location(), "--spring.jpa.open-in-view=false")) {
            assertEquals("false", context.getEnvironment().getProperty("spring.jpa.open-in-view"));
            assertEquals(1, CREATED.get());
        }
    }

    @Test
    void activeProfileAndNativeHibernatePropertyMustAlsoFailEarly() throws Exception {
        CREATED.set(0);
        Files.writeString(dir.resolve("application.yml"), "spring:\n  profiles:\n    active: bad\n  jpa:\n    hibernate:\n      ddl-auto: none\n");
        Files.writeString(dir.resolve("application-bad.yml"), "spring:\n  jpa:\n    hibernate:\n      ddl-auto: create-drop\n");
        assertThrows(RuntimeException.class, () -> app(MarkerConfiguration.class).run(location()));
        assertEquals(0, CREATED.get());
        Files.delete(dir.resolve("application-bad.yml"));
        Files.writeString(dir.resolve("application.yml"), "spring:\n  jpa:\n    hibernate:\n      ddl-auto: none\n    properties:\n      hibernate.hbm2ddl.auto: create\n");
        assertThrows(RuntimeException.class, () -> app(MarkerConfiguration.class).run(location()));
        assertEquals(0, CREATED.get());
    }

    @Test
    void nativeDdlAutoEntrypointsMustNotBypassEarlyValidation() throws Exception {
        Files.writeString(dir.resolve("application.yml"), "spring:\n  jpa:\n    hibernate:\n      ddl-auto: none\n");
        for (String key : new String[]{"hibernate.hbm2ddl.auto", "spring.jpa.properties.hibernate.hbm2ddl.auto",
                "spring.jpa.properties[hibernate.hbm2ddl.auto]"}) {
            for (String action : new String[]{"create", "create-drop", "drop", "validate"}) {
                CREATED.set(0);
                RuntimeException error = assertThrows(RuntimeException.class,
                        () -> app(MarkerConfiguration.class).run(location(), "--" + key + "=" + action));
                assertTrue(messages(error).contains("hibernate.hbm2ddl.auto"), messages(error));
                assertEquals(0, CREATED.get());
            }
            for (String action : new String[]{"none", "update", "NONE", "UpDaTe", "  NoNe  ", "  uPdAtE  "}) {
                try (var context = app(MarkerConfiguration.class).run(location(), "--" + key + "=" + action)) {
                    assertTrue(context.isActive());
                }
            }
        }
    }

    @Test
    void safeActionsAndUnconfiguredDefaultMustStartRealHibernate() throws Exception {
        Files.writeString(dir.resolve("application.yml"), "spring:\n  datasource:\n    url: jdbc:h2:mem:ddl_guard;DB_CLOSE_DELAY=-1\n    driver-class-name: org.h2.Driver\n  jpa:\n    database-platform: org.hibernate.dialect.H2Dialect\n");
        for (String action : new String[]{"none", "update", "default"}) {
            String[] args = action.equals("default") ? new String[]{location()}
                    : new String[]{location(), "--spring.jpa.hibernate.ddl-auto=" + action};
            try (var context = app(HibernateConfiguration.class).run(args)) {
                assertTrue(context.getBean(EntityManagerFactory.class).isOpen());
                assertEquals("false", context.getEnvironment().getProperty("spring.jpa.open-in-view"));
                assertEquals(action.equals("default") ? "none" : action,
                        context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
            }
        }
    }

    @Test
    void defaultsMustNotOverrideConfiguredUpdateAndJpaActionsMustAllowUpdate() throws Exception {
        Files.writeString(dir.resolve("application.yml"), "spring:\n  jpa:\n    hibernate:\n      ddl-auto: update\n");
        try (var context = app(MarkerConfiguration.class).run(location())) {
            assertEquals("update", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
            assertEquals("false", context.getEnvironment().getProperty("spring.jpa.open-in-view"));
        }
        for (String key : new String[]{"jakarta.persistence.schema-generation.database.action",
                "javax.persistence.schema-generation.database.action"}) {
            for (String action : new String[]{"create", "drop", "drop-and-create", "validate"}) {
                CREATED.set(0);
                RuntimeException error = assertThrows(RuntimeException.class,
                        () -> app(MarkerConfiguration.class).run(location(), "--spring.jpa.properties." + key + "=" + action));
                assertTrue(messages(error).contains(key), messages(error));
                assertEquals(0, CREATED.get());
            }
            for (String action : new String[]{"none", "update", "NONE", "UpDaTe", "  NoNe  ", "  uPdAtE  "}) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(key, action);
                assertDoesNotThrow(() -> JpaDdlAutoSafetyConfiguration.simpleDaoDdlAutoSafetyCustomizer().customize(properties));
                assertEquals(action, properties.get(key), "校验不能修改原始配置值");
            }
            // 独立的空数据库 + hbm2ddl none，证明标准参数上的 update 真正执行了结构更新。
            String url = "jdbc:h2:mem:ddl_guard_" + java.util.UUID.randomUUID().toString().replace("-", "");
            try (var context = app(HibernateConfiguration.class).run(location(),
                    "--spring.datasource.url=" + url, "--spring.datasource.driver-class-name=org.h2.Driver",
                    "--spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                    "--spring.jpa.hibernate.ddl-auto=none", "--spring.jpa.properties." + key + "=update")) {
                assertTrue(context.getBean(EntityManagerFactory.class).isOpen());
                assertEquals("none", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
                try (var connection = context.getBean(javax.sql.DataSource.class).getConnection();
                     var tables = connection.getMetaData().getTables(null, null, "DDL_GUARD_PROBE", new String[]{"TABLE"})) {
                    assertTrue(tables.next(), key + "=update 必须创建实体表");
                }
            }
        }
    }

    @Test
    void finalHibernateSettingsMustRejectLateOverridesAndJpaSchemaActions() {
        for (String action : new String[]{"create", "create-drop", "validate", "drop"}) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto", action);
            assertThrows(IllegalStateException.class,
                    () -> JpaDdlAutoSafetyConfiguration.simpleDaoDdlAutoSafetyCustomizer().customize(properties));
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
        assertThrows(IllegalStateException.class,
                () -> JpaDdlAutoSafetyConfiguration.simpleDaoDdlAutoSafetyCustomizer().customize(properties));
    }

    private SpringApplication app(Class<?> source) {
        SpringApplication app = new SpringApplication(source);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setRegisterShutdownHook(false);
        app.setDefaultProperties(Map.of("spring.main.banner-mode", "off", "logging.level.root", "ERROR"));
        return app;
    }
    private String location() { return "--spring.config.location=file:" + dir.toAbsolutePath() + "/"; }
    private static String messages(Throwable error) {
        StringBuilder text = new StringBuilder();
        while (error != null) { text.append(error.getMessage()).append('\n'); error = error.getCause(); }
        return text.toString();
    }
    @Configuration(proxyBeanMethods = false)
    static class MarkerConfiguration {
        @Bean String marker() { CREATED.incrementAndGet(); return "created"; }
    }
    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JpaDdlAutoSafetyConfiguration.class})
    @EntityScan(basePackageClasses = GuardEntity.class)
    static class HibernateConfiguration {}

    @Entity(name = "DdlGuardEntity")
    @Table(name = "ddl_guard_probe")
    public static class GuardEntity {
        @Id Long id;
    }
}
