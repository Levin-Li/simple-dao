package com.levin.commons.dao.support.hibernate;

import org.hibernate.Version;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.SelfRenderingSqlFragmentExpression;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.FunctionExpression;
import org.hibernate.sql.ast.tree.expression.SelfRenderingExpression;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativePostgreSQLJsonArrayAppendFunctionProbeTest {

    @Test
    void shouldReadHibernateRuntimeVersionDynamically() {
        assertEquals(Version.getVersionString(), SimpleDaoHibernateFunctionContributor.getHibernateVersion());
    }

    @Test
    void shouldDetectRuntimeHibernateRootPathRenderingBug() {
        assertTrue(NativePostgreSQLJsonArrayAppendFunctionProbe.isRootPathRenderingBroken(new TypeConfiguration()));
    }

    @Test
    void shouldRequireOverrideWhenRootPathUsesJsonbSetLaxWithEmptyPath() {
        assertTrue(NativePostgreSQLJsonArrayAppendFunctionProbe.isRenderingBroken(
                "jsonb_set_lax(t.d,t.p,(t.d)#>t.p||to_jsonb(?),false,'return_target')"
                        + " from (values(doc,array[]::text[])) t(d,p)"
        ));
    }

    @Test
    void shouldNotRequireOverrideWhenRootPathRendersDirectConcatenation() {
        assertFalse(NativePostgreSQLJsonArrayAppendFunctionProbe.isRenderingBroken(
                "json_doc||to_jsonb(append_value)"
        ));
    }

    @Test
    void workaroundShouldRenderCoalescedEmptyJsonArrayAsJsonb() {
        StringBuilder sql = new StringBuilder();
        SqlAppender appender = sql::append;
        Expression json = new SelfRenderingSqlFragmentExpression("coalesce(action_log,json_array())");
        Expression path = expression("'$'", "$");
        SqlAstNode value = node("?1");
        SqlAstTranslator<?> translator = translator(sql, path);

        new SimpleDaoPostgreSQLJsonArrayAppendFunction(new TypeConfiguration()).render(
                appender,
                List.of(json, path, value),
                (ReturnableType<?>) null,
                translator
        );

        assertFalse(sql.toString().contains("json_array()"), sql.toString());
        assertTrue(sql.toString().contains("jsonb_build_array()"), sql.toString());
        assertFalse(sql.toString().contains("jsonb_set_lax"), sql.toString());
        assertTrue(sql.toString().contains("||to_jsonb"), sql.toString());
    }

    @Test
    void workaroundShouldRenderCoalescedFunctionExpressionEmptyJsonArrayAsJsonb() {
        StringBuilder sql = new StringBuilder();
        SqlAppender appender = sql::append;
        Expression json = functionExpression(
                "coalesce",
                "coalesce(action_log,json_array())",
                List.of(node("action_log"), functionExpression("json_array", "json_array()", List.of()))
        );
        Expression path = expression("'$'", "$");
        SqlAstNode value = node("?1");
        SqlAstTranslator<?> translator = translator(sql, path);

        new SimpleDaoPostgreSQLJsonArrayAppendFunction(new TypeConfiguration()).render(
                appender,
                List.of(json, path, value),
                (ReturnableType<?>) null,
                translator
        );

        assertFalse(sql.toString().contains("json_array()"), sql.toString());
        assertTrue(sql.toString().contains("jsonb_build_array()"), sql.toString());
        assertFalse(sql.toString().contains("jsonb_set_lax"), sql.toString());
        assertTrue(sql.toString().contains("||to_jsonb"), sql.toString());
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

    private static Expression expression(String sql, Object literalValue) {
        return (Expression) Proxy.newProxyInstance(
                Expression.class.getClassLoader(),
                new Class<?>[]{Expression.class},
                nodeInvocationHandler(sql, literalValue)
        );
    }

    private static SqlAstNode node(String sql) {
        return (SqlAstNode) Proxy.newProxyInstance(
                SqlAstNode.class.getClassLoader(),
                new Class<?>[]{SqlAstNode.class},
                nodeInvocationHandler(sql, null)
        );
    }

    private static Expression functionExpression(
            String functionName,
            String sql,
            List<? extends SqlAstNode> arguments) {
        return (Expression) Proxy.newProxyInstance(
                Expression.class.getClassLoader(),
                new Class<?>[]{Expression.class, FunctionExpression.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getFunctionName".equals(methodName)) {
                        return functionName;
                    }
                    if ("getArguments".equals(methodName)) {
                        return arguments;
                    }
                    return nodeInvocationHandler(sql, null).invoke(proxy, method, args);
                }
        );
    }

    private static InvocationHandler nodeInvocationHandler(String sql, Object literalValue) {
        return (proxy, method, args) -> {
            String methodName = method.getName();
            if ("accept".equals(methodName)) {
                ((SqlAppender) args[0]).appendSql(sql);
                return null;
            }
            if ("getExpressionType".equals(methodName)) {
                return null;
            }
            if ("toString".equals(methodName)) {
                return sql;
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == args[0];
            }
            if ("getLiteralValue".equals(methodName)) {
                return literalValue;
            }
            return defaultValue(method.getReturnType());
        };
    }

    private static SqlAstTranslator<?> translator(StringBuilder sql, Expression path) {
        return (SqlAstTranslator<?>) Proxy.newProxyInstance(
                SqlAstTranslator.class.getClassLoader(),
                new Class<?>[]{SqlAstTranslator.class, SqlAppender.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getLiteralValue".equals(methodName)) {
                        return args[0] == path ? "$" : null;
                    }
                    if ("appendSql".equals(methodName)) {
                        sql.append(args[0]);
                        return null;
                    }
                    if ("append".equals(methodName)) {
                        sql.append(args[0]);
                        return proxy;
                    }
                    if ("visitSelfRenderingExpression".equals(methodName)) {
                        ((SelfRenderingExpression) args[0]).renderToSql(
                                (SqlAppender) proxy,
                                (SqlAstTranslator<?>) proxy,
                                null
                        );
                        return null;
                    }
                    if ("toString".equals(methodName)) {
                        return "json_array_append_test_translator";
                    }
                    if ("hashCode".equals(methodName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(methodName)) {
                        return proxy == args[0];
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class == returnType) {
            return false;
        }
        if (char.class == returnType) {
            return '\0';
        }
        if (byte.class == returnType) {
            return (byte) 0;
        }
        if (short.class == returnType) {
            return (short) 0;
        }
        if (int.class == returnType) {
            return 0;
        }
        if (long.class == returnType) {
            return 0L;
        }
        if (float.class == returnType) {
            return 0F;
        }
        if (double.class == returnType) {
            return 0D;
        }
        return null;
    }
}
