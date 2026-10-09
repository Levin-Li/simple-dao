package com.levin.commons.dao.support.hibernate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.Collections;
import java.util.Map;

/**
 * 配置文件加载后、创建容器前校验 JPA 安全配置。
 * create/create-drop/drop 等 DDL 动作可能因配置错误删除或重建业务数据表，因此按项目约定
 * 只允许 none/update；validate 等其他动作也不属于该白名单。
 * Open In View 会延长持久化上下文的生命周期，可能让请求处理或序列化阶段触发事务外的
 * 延迟加载和额外查询，因此必须关闭，让数据加载在服务层明确完成。
 * 必须在创建数据源和 EntityManagerFactory 之前检查：在 Bean 初始化之后检查，
 * Hibernate 可能已经执行了破坏性的 DDL，无法保护原有数据。
 */
public class HibernateDdlAutoGuard implements EnvironmentPostProcessor, Ordered {
    static final String OPEN_IN_VIEW = "spring.jpa.open-in-view";
    static final String DDL_AUTO = "spring.jpa.hibernate.ddl-auto";

    @Override public int getOrder() {
        // 必须先读取 application.yml、活动 profile 和配置导入，再立即校验其有效值。
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Binder binder = Binder.get(environment);
        String openInView = binder.bind(OPEN_IN_VIEW, String.class).orElse(null);
        if (openInView != null && !"false".equalsIgnoreCase(openInView)) {
            throw new IllegalStateException("Simple DAO 的配置 " + OPEN_IN_VIEW + "=" + openInView
                    + " 不安全，必须为 false");
        }
        String action = binder.bind(DDL_AUTO, String.class).orElse(null);
        validate(DDL_AUTO, action);
        Map<String, String> properties = binder.bind("spring.jpa.properties", Bindable.mapOf(String.class, String.class))
                .orElse(Collections.emptyMap());
        validateProperties(properties);
        validate("hibernate.hbm2ddl.auto", environment.getProperty("hibernate.hbm2ddl.auto"));
        // 显式配置仍保持原有优先级；缺省值关闭 OSIV，避免 Boot 为内存数据库推导 create-drop。
        environment.getPropertySources().addLast(new MapPropertySource("simpleDaoSafeJpaDefaults",
                Map.of(DDL_AUTO, "none", OPEN_IN_VIEW, "false")));
    }

    static void validateProperties(Map<?, ?> properties) {
        validate("hibernate.hbm2ddl.auto", properties.get("hibernate.hbm2ddl.auto"));
        for (String key : new String[]{"jakarta.persistence.schema-generation.database.action",
                "javax.persistence.schema-generation.database.action"}) {
            // Hibernate 在 JPA 标准属性上也支持 update 扩展，使用同一白名单避免限制过严。
            validate(key, properties.get(key));
        }
    }

    static void validate(String key, Object action) {
        if (action != null && !"none".equals(action.toString()) && !"update".equals(action.toString())) {
            throw new IllegalStateException("Simple DAO 的 Hibernate 配置 " + key + "=" + action
                    + " 不安全，只允许 none 或 update");
        }
    }
}
