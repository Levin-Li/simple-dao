package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.function.json.PostgreSQLJsonArrayAppendFunction;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.Literal;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;

/**
 * Fixes Hibernate 7.2.7 PostgreSQL root-path rendering for json_array_append(..., '$', ...).
 */
class SimpleDaoPostgreSQLJsonArrayAppendFunction extends PostgreSQLJsonArrayAppendFunction {

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
        json.accept(translator);
        if (needsCast) {
            sqlAppender.appendSql(" as jsonb)");
        }
    }

    private static void renderAppendValue(
            SqlAppender sqlAppender,
            SqlAstNode value,
            SqlAstTranslator<?> translator) {
        sqlAppender.appendSql("jsonb_build_array(");
        value.accept(translator);
        if (value instanceof Literal literal && literal.getJdbcMapping().getJdbcType().isString()) {
            sqlAppender.appendSql("::text");
        }
        sqlAppender.appendSql(')');
    }

    private static boolean isJsonType(Expression expression) {
        JdbcMappingContainer expressionType = expression.getExpressionType();
        return expressionType != null && expressionType.getSingleJdbcMapping().getJdbcType().isJson();
    }
}
