package com.levin.commons.dao.support;

import com.levin.commons.dao.support.hibernate.SimpleDaoHibernateFunctionContributor;
import com.levin.commons.dao.support.hibernate.SimpleDaoPostgreSQLDialect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLJsonArrayAppendFunctionTest {

    @Test
    void rootPathShouldRenderPostgreSQLAppendWithoutJsonbSetPathMutation() {
        CapturingStatementInspector statementInspector = new CapturingStatementInspector();

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, SimpleDaoPostgreSQLDialect.class)
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:pg_json_array_append;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .applySetting(AvailableSettings.STATEMENT_INSPECTOR, statementInspector)
                .applySetting("hibernate.query.hql.json_functions_enabled", true)
                .build();

        try {
            try (SessionFactory sessionFactory = new MetadataSources(serviceRegistry)
                    .addAnnotatedClass(PgJsonEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
                 Session session = sessionFactory.openSession()) {

                assertThrows(RuntimeException.class, () -> session
                        .createSelectionQuery(
                                "select json_array_append(e.roles, '$', :role) from PgJsonEntity e",
                                String.class)
                        .setParameter("role", "admin")
                        .getResultList());
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }

        String sql = statementInspector.lastSql();
        assertNotNull(sql, "Hibernate should render SQL before the missing H2 table error");
        assertFalse(sql.contains("array]::text[]"), sql);

        if (SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonArrayAppendOverride()) {
            assertTrue(sql.contains("json_array_append") || sql.contains("jsonb"), sql);
            assertTrue(sql.contains("||"), sql);
            assertFalse(sql.contains("jsonb_set"), sql);
            assertFalse(sql.contains("jsonb_set_lax"), sql);
        }
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
