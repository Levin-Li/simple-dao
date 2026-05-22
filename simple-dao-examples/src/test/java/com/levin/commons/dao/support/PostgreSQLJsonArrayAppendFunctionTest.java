package com.levin.commons.dao.support;

import com.levin.commons.dao.support.hibernate.SimpleDaoHibernateAutoConfiguration;
import com.levin.commons.dao.support.hibernate.SimpleDaoHibernateFunctionContributor;
import com.levin.commons.dao.support.hibernate.SimpleDaoPostgreSQLDialect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLJsonArrayAppendFunctionTest {

    @Test
    void rootPathShouldRenderPostgreSQLAppendWithEmptyPathArray() {
        assertRootPathAppendSql(SimpleDaoPostgreSQLDialect.class);
    }

    @Test
    void rootPathShouldRenderCoalescedEmptyJsonArrayWithEmptyPathArray() {
        String sql = renderRootPathAppendSql(
                SimpleDaoPostgreSQLDialect.class,
                "select json_array_append(coalesce(e.roles, json_array()), '$', :role) from PgJsonEntity e"
        );

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertFalse(sql.contains("jsonb_set_lax"), sql);
            assertFalse(sql.contains("array[]::text[]"), sql);
            assertFalse(sql.contains("json_array()"), sql);
            assertTrue(sql.contains("jsonb_build_array()"), sql);
            assertTrue(sql.contains("||to_jsonb"), sql);
            assertFalse(sql.contains("array]::text[]"), sql);
        }
    }

    @Test
    void rootPathShouldRenderJsonColumnCoalescedEmptyArrayAsJsonb() {
        String sql = renderRootPathAppendSql(
                SimpleDaoPostgreSQLDialect.class,
                PgJsonColumnEntity.class,
                "select json_array_append(coalesce(e.actionLog, json_array()), '$', :role) from PgJsonColumnEntity e"
        );

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertFalse(sql.contains("jsonb_set_lax"), sql);
            assertFalse(sql.contains("array[]::text[]"), sql);
            assertFalse(sql.contains("json_array()"), sql);
            assertTrue(sql.contains("jsonb_build_array()"), sql);
            assertTrue(sql.contains("||to_jsonb"), sql);
        }
    }

    @Test
    void rootPathUpdateShouldRenderCoalescedEmptyArrayAsJsonb() {
        String sql = renderRootPathMutationSql(
                SimpleDaoPostgreSQLDialect.class,
                PgJsonColumnEntity.class,
                "update PgJsonColumnEntity e set e.actionLog = json_array_append(coalesce(e.actionLog, json_array()), '$', :role)"
        );

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertFalse(sql.contains("jsonb_set_lax"), sql);
            assertFalse(sql.contains("array[]::text[]"), sql);
            assertFalse(sql.contains("json_array()"), sql);
            assertTrue(sql.contains("jsonb_build_array()"), sql);
            assertTrue(sql.contains("||to_jsonb"), sql);
        }
    }

    @Test
    void rootPathShouldRenderUntypedCoalescedEmptyArrayAsJsonb() {
        String sql = renderRootPathAppendSql(
                SimpleDaoPostgreSQLDialect.class,
                PgUntypedJsonColumnEntity.class,
                "select json_array_append(coalesce(e.actionLog, json_array()), '$', :role) from PgUntypedJsonColumnEntity e"
        );

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertFalse(sql.contains("jsonb_set_lax"), sql);
            assertFalse(sql.contains("array[]::text[]"), sql);
            assertFalse(sql.contains("json_array()"), sql);
            assertTrue(sql.contains("jsonb_build_array()"), sql);
            assertTrue(sql.contains("||to_jsonb"), sql);
        }
    }

    @Test
    void springBootCustomizerShouldRewriteExplicitNativePostgreSQLDialect() {
        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put(AvailableSettings.DIALECT, PostgreSQLDialect.class.getName());

        SimpleDaoHibernateAutoConfiguration.postgreSQLJsonFunctionHibernatePropertiesCustomizer()
                .customize(hibernateProperties);

        Object dialect = hibernateProperties.get(AvailableSettings.DIALECT);
        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertTrue(SimpleDaoPostgreSQLDialect.class.getName().equals(dialect), String.valueOf(dialect));
        }
        assertRootPathAppendSql(dialect);
    }

    private static void assertRootPathAppendSql(Object dialect) {
        String sql = renderRootPathAppendSql(
                dialect,
                "select json_array_append(e.roles, '$', :role) from PgJsonEntity e"
        );

        assertFalse(sql.contains("array]::text[]"), sql);

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertTrue(sql.contains("json_array_append") || sql.contains("jsonb"), sql);
            assertFalse(sql.contains("jsonb_set_lax"), sql);
            assertFalse(sql.contains("array[]::text[]"), sql);
            assertTrue(sql.contains("||to_jsonb"), sql);
        }
    }

    private static String renderRootPathAppendSql(Object dialect, String hql) {
        return renderRootPathAppendSql(dialect, PgJsonEntity.class, hql);
    }

    private static String renderRootPathAppendSql(Object dialect, Class<?> entityClass, String hql) {
        CapturingStatementInspector statementInspector = new CapturingStatementInspector();

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialect)
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:pg_json_array_append;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .applySetting(AvailableSettings.STATEMENT_INSPECTOR, statementInspector)
                .applySetting("hibernate.query.hql.json_functions_enabled", true)
                .build();

        try {
            try (SessionFactory sessionFactory = new MetadataSources(serviceRegistry)
                    .addAnnotatedClass(entityClass)
                    .buildMetadata()
                    .buildSessionFactory();
                 Session session = sessionFactory.openSession()) {

                assertThrows(RuntimeException.class, () -> session
                        .createSelectionQuery(
                                hql,
                                String.class)
                        .setParameter("role", "admin")
                        .getResultList());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }

        String sql = statementInspector.lastSql();
        assertNotNull(sql, "Hibernate should render SQL before the missing H2 table error");
        return sql;
    }

    private static String renderRootPathMutationSql(Object dialect, Class<?> entityClass, String hql) {
        CapturingStatementInspector statementInspector = new CapturingStatementInspector();

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialect)
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:pg_json_array_append_mutation;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .applySetting(AvailableSettings.STATEMENT_INSPECTOR, statementInspector)
                .applySetting("hibernate.query.hql.json_functions_enabled", true)
                .build();

        try {
            try (SessionFactory sessionFactory = new MetadataSources(serviceRegistry)
                    .addAnnotatedClass(entityClass)
                    .buildMetadata()
                    .buildSessionFactory();
                 Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                assertThrows(RuntimeException.class, () -> session
                        .createMutationQuery(hql)
                        .setParameter("role", "admin")
                        .executeUpdate());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }

        String sql = statementInspector.lastSql();
        assertNotNull(sql, "Hibernate should render mutation SQL before the missing H2 table error");
        return sql;
    }

    @Entity(name = "PgJsonEntity")
    @Table(name = "pg_json_entity")
    static class PgJsonEntity {

        @Id
        Long id;

        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        List<String> roles;
    }

    @Entity(name = "PgJsonColumnEntity")
    @Table(name = "pg_json_column_entity")
    static class PgJsonColumnEntity {

        @Id
        Long id;

        @JdbcTypeCode(SqlTypes.JSON)
        @Column(name = "action_log", columnDefinition = "json")
        List<String> actionLog;
    }

    @Entity(name = "PgUntypedJsonColumnEntity")
    @Table(name = "pg_untyped_json_column_entity")
    static class PgUntypedJsonColumnEntity {

        @Id
        Long id;

        @Column(name = "action_log", columnDefinition = "jsonb")
        String actionLog;
    }

    private static class CapturingStatementInspector implements StatementInspector {

        private final List<String> sqlList = new ArrayList<>();

        @Override
        public String inspect(String sql) {
            sqlList.add(sql);
            return sql;
        }

        String lastSql() {
            if (sqlList.isEmpty()) {
                return null;
            }
            return sqlList.get(sqlList.size() - 1);
        }
    }
}
