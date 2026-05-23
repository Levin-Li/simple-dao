package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.function.json.PostgreSQLJsonArrayInsertFunction;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.SelfRenderingExpression;
import org.hibernate.sql.ast.tree.expression.SelfRenderingSqlFragmentExpression;
import org.hibernate.type.spi.TypeConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;

/**
 * Detects whether Hibernate's native PostgreSQL json_array_insert rendering still needs the workaround.
 */
final class NativePostgreSQLJsonArrayInsertFunctionProbe {

    private NativePostgreSQLJsonArrayInsertFunctionProbe() {
    }

    static boolean isCoalesceRenderingBroken(TypeConfiguration typeConfiguration) {
        try {
            String sql = renderNativeCoalesceInsert(typeConfiguration);
            return isRenderingBroken(sql);
        } catch (RuntimeException ex) {
            return true;
        }
    }

    static boolean isRenderingBroken(String sql) {
        String normalized = sql.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return normalized.contains("jsonb_insert(")
                && normalized.contains("coalesce(")
                && normalized.contains("json_array()");
    }

    private static String renderNativeCoalesceInsert(TypeConfiguration typeConfiguration) {
        StringBuilder sql = new StringBuilder();
        SqlAppender appender = sql::append;
        Expression json = new SelfRenderingSqlFragmentExpression("coalesce(action_log,json_array())");
        Expression path = expression("'$[0]'", "$[0]");
        SqlAstNode value = node("insert_value");
        SqlAstTranslator<?> translator = translator(path, appender);

        new PostgreSQLJsonArrayInsertFunction(typeConfiguration).render(
                appender,
                List.of(json, path, value),
                (ReturnableType<?>) null,
                translator
        );

        return sql.toString();
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

    private static InvocationHandler nodeInvocationHandler(String sql, Object literalValue) {
        return (proxy, method, args) -> {
            String methodName = method.getName();
            if ("accept".equals(methodName)) {
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
            return NativePostgreSQLJsonArrayAppendFunctionProbe.defaultValue(method.getReturnType());
        };
    }

    private static SqlAstTranslator<?> translator(Expression path, SqlAppender appender) {
        return (SqlAstTranslator<?>) Proxy.newProxyInstance(
                SqlAstTranslator.class.getClassLoader(),
                new Class<?>[]{SqlAstTranslator.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getLiteralValue".equals(methodName)) {
                        return args[0] == path ? "$[0]" : null;
                    }
                    if ("visitSelfRenderingExpression".equals(methodName)) {
                        ((SelfRenderingExpression) args[0]).renderToSql(appender, (SqlAstTranslator<?>) proxy, null);
                        return null;
                    }
                    if ("toString".equals(methodName)) {
                        return "json_array_insert_probe_translator";
                    }
                    if ("hashCode".equals(methodName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(methodName)) {
                        return proxy == args[0];
                    }
                    return NativePostgreSQLJsonArrayAppendFunctionProbe.defaultValue(method.getReturnType());
                }
        );
    }
}
