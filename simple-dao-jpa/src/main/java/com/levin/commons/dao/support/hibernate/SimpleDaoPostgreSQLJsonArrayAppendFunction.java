package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.function.json.PostgreSQLJsonArrayAppendFunction;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.FunctionExpression;
import org.hibernate.sql.ast.tree.expression.Literal;
import org.hibernate.sql.ast.tree.expression.SelfRenderingSqlFragmentExpression;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Fixes Hibernate PostgreSQL root-path rendering for json_array_append(..., '$', ...) when the runtime probe detects the bug.
 */
class SimpleDaoPostgreSQLJsonArrayAppendFunction extends PostgreSQLJsonArrayAppendFunction {

    private static final Pattern EMPTY_JSON_ARRAY_FUNCTION =
            Pattern.compile("\\bjson_array\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE);

    SimpleDaoPostgreSQLJsonArrayAppendFunction(TypeConfiguration typeConfiguration) {
        super(true, typeConfiguration);
    }

    @Override
    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> arguments,
            ReturnableType<?> returnType,
            SqlAstTranslator<?> translator) {
        Expression jsonPath = (Expression) arguments.get(1);
        Object literalPath = translator.getLiteralValue(jsonPath);

        if (!"$".equals(literalPath)) {
            super.render(sqlAppender, arguments, returnType, translator);
            return;
        }

        Expression json = (Expression) arguments.get(0);
        SqlAstNode value = arguments.get(2);

        sqlAppender.appendSql('(');
        renderJsonDocument(sqlAppender, json, translator);
        sqlAppender.appendSql("||");
        renderAppendValue(sqlAppender, value, translator);
        sqlAppender.appendSql(')');
    }

    private static void renderJsonDocument(
            SqlAppender sqlAppender,
            Expression json,
            SqlAstTranslator<?> translator) {
        boolean needsCast = !isJsonType(json);
        if (needsCast) {
            sqlAppender.appendSql("cast(");
        }
        renderJsonDocumentExpression(sqlAppender, json, translator);
        if (needsCast) {
            sqlAppender.appendSql(" as jsonb)");
        }
    }

    private static void renderJsonDocumentExpression(
            SqlAppender sqlAppender,
            Expression json,
            SqlAstTranslator<?> translator) {
        if (isEmptyJsonArrayFunction(json)) {
            sqlAppender.appendSql("jsonb_build_array()");
            return;
        }

        if (json instanceof FunctionExpression functionExpression
                && "coalesce".equalsIgnoreCase(functionExpression.getFunctionName())) {
            renderCoalesceJsonDocumentExpression(sqlAppender, functionExpression, translator);
            return;
        }

        if (json instanceof SelfRenderingSqlFragmentExpression sqlFragmentExpression) {
            sqlAppender.appendSql(forceEmptyJsonArrayToJsonb(sqlFragmentExpression.getExpression()));
            return;
        }

        json.accept(translator);
    }

    private static void renderCoalesceJsonDocumentExpression(
            SqlAppender sqlAppender,
            FunctionExpression coalesceExpression,
            SqlAstTranslator<?> translator) {
        sqlAppender.appendSql("coalesce(");
        char separator = ' ';
        for (SqlAstNode argument : coalesceExpression.getArguments()) {
            if (separator != ' ') {
                sqlAppender.appendSql(separator);
            }
            if (argument instanceof Expression expression) {
                renderJsonDocumentExpression(sqlAppender, expression, translator);
            } else {
                argument.accept(translator);
            }
            separator = ',';
        }
        sqlAppender.appendSql(')');
    }

    private static boolean isEmptyJsonArrayFunction(Expression expression) {
        return expression instanceof FunctionExpression functionExpression
                && "json_array".equalsIgnoreCase(functionExpression.getFunctionName())
                && functionExpression.getArguments().isEmpty();
    }

    private static String forceEmptyJsonArrayToJsonb(String sql) {
        return EMPTY_JSON_ARRAY_FUNCTION.matcher(sql).replaceAll("jsonb_build_array()");
    }

    private static void renderAppendValue(
            SqlAppender sqlAppender,
            SqlAstNode value,
            SqlAstTranslator<?> translator) {
        if (value instanceof Literal literal && literal.getLiteralValue() == null) {
            sqlAppender.appendSql("null::jsonb");
        } else {
            sqlAppender.appendSql("to_jsonb(");
            value.accept(translator);
            if (value instanceof Literal literal && literal.getJdbcMapping().getJdbcType().isString()) {
                sqlAppender.appendSql("::text");
            }
            sqlAppender.appendSql(')');
        }
    }

    private static boolean isJsonType(Expression expression) {
        JdbcMappingContainer expressionType = expression.getExpressionType();
        return expressionType != null && expressionType.getSingleJdbcMapping().getJdbcType().isJson();
    }
}
