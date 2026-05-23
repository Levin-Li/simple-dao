package com.levin.commons.dao.support.hibernate;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.json.JsonPathHelper;
import org.hibernate.dialect.function.json.PostgreSQLJsonArrayInsertFunction;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;

/**
 * Fixes PostgreSQL json_array_insert rendering when Hibernate leaves json_array() in a jsonb_insert document expression.
 */
class SimpleDaoPostgreSQLJsonArrayInsertFunction extends PostgreSQLJsonArrayInsertFunction {

    SimpleDaoPostgreSQLJsonArrayInsertFunction(TypeConfiguration typeConfiguration) {
        super(typeConfiguration);
    }

    @Override
    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> arguments,
            ReturnableType<?> returnType,
            SqlAstTranslator<?> translator) {
        Expression json = (Expression) arguments.get(0);
        Expression jsonPath = (Expression) arguments.get(1);
        SqlAstNode value = arguments.get(2);

        sqlAppender.appendSql("jsonb_insert(");
        SimpleDaoPostgreSQLJsonArrayAppendFunction.renderJsonDocument(sqlAppender, json, translator);
        sqlAppender.appendSql(',');
        renderPath(sqlAppender, jsonPath, translator);
        sqlAppender.appendSql(',');
        SimpleDaoPostgreSQLJsonArrayAppendFunction.renderJsonValue(sqlAppender, value, translator);
        sqlAppender.appendSql(')');
    }

    private static void renderPath(
            SqlAppender sqlAppender,
            Expression jsonPath,
            SqlAstTranslator<?> translator) {
        List<JsonPathHelper.JsonPathElement> jsonPathElements =
                JsonPathHelper.parseJsonPathElements((String) translator.getLiteralValue(jsonPath));

        sqlAppender.appendSql("array");
        char separator = '[';
        for (JsonPathHelper.JsonPathElement element : jsonPathElements) {
            sqlAppender.appendSql(separator);
            if (element instanceof JsonPathHelper.JsonAttribute attribute) {
                sqlAppender.appendSingleQuoteEscapedString(attribute.attribute());
            } else if (element instanceof JsonPathHelper.JsonParameterIndexAccess parameterIndexAccess) {
                throw new QueryException("JSON path [" + jsonPath + "] uses unsupported parameter index access ["
                        + parameterIndexAccess.parameterName() + "]");
            } else {
                sqlAppender.appendSql('\'');
                sqlAppender.appendSql(((JsonPathHelper.JsonIndexAccess) element).index());
                sqlAppender.appendSql('\'');
            }
            separator = ',';
        }
        sqlAppender.appendSql("]::text[]");
    }
}
