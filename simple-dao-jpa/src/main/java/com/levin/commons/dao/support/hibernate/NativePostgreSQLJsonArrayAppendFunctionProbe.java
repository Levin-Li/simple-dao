package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.function.json.PostgreSQLJsonArrayAppendFunction;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.type.spi.TypeConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Detects whether Hibernate's native PostgreSQL json_array_append root-path rendering still needs the workaround.
 */
final class NativePostgreSQLJsonArrayAppendFunctionProbe {

    private NativePostgreSQLJsonArrayAppendFunctionProbe() {
    }

    static boolean isRootPathRenderingBroken(TypeConfiguration typeConfiguration) {
        try {
            String sql = renderNativeRootPathAppend(typeConfiguration);
            return isRenderingBroken(sql);
        } catch (RuntimeException ex) {
            return true;
        }
    }

    static boolean isRenderingBroken(String sql) {
        return sql.contains("array]::text[]");
    }

    private static String renderNativeRootPathAppend(TypeConfiguration typeConfiguration) {
        StringBuilder sql = new StringBuilder();
        SqlAppender appender = sql::append;
        Expression json = expression("json_doc", null);
        Expression path = expression("'$'", "$");
        SqlAstNode value = node("append_value");
        SqlAstTranslator<?> translator = translator(path);

        new PostgreSQLJsonArrayAppendFunction(true, typeConfiguration).render(
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
            return defaultValue(method.getReturnType());
        };
    }

    private static SqlAstTranslator<?> translator(Expression path) {
        return (SqlAstTranslator<?>) Proxy.newProxyInstance(
                SqlAstTranslator.class.getClassLoader(),
                new Class<?>[]{SqlAstTranslator.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getLiteralValue".equals(methodName)) {
                        return args[0] == path ? "$" : null;
                    }
                    if ("toString".equals(methodName)) {
                        return "json_array_append_probe_translator";
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
