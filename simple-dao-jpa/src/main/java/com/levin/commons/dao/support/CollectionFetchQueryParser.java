package com.levin.commons.dao.support;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.hibernate.grammars.hql.HqlParser;
import org.hibernate.query.hql.internal.HqlParseTreeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 使用 Hibernate HQL 语法树定位原文片段，避免自行解析 JPQL。 */
final class CollectionFetchQueryParser {
    private CollectionFetchQueryParser() {
    }

    static CollectionFetchQueryPlan.Parts parse(String statement) {
        HqlParser parser = HqlParseTreeBuilder.INSTANCE.buildHqlParser(statement,
                HqlParseTreeBuilder.INSTANCE.buildHqlLexer(statement));
        HqlParser.StatementContext parsed = parser.statement();
        if (parser.getNumberOfSyntaxErrors() != 0 || parsed.selectStatement() == null) {
            throw unsupported("要求 SELECT 查询");
        }
        HqlParser.QueryExpressionContext expression = parsed.selectStatement().queryExpression();
        if (expression.withClause() != null || !expression.setOperator().isEmpty()
                || expression.orderedQuery().size() != 1
                || !(expression.orderedQuery(0) instanceof HqlParser.QuerySpecExpressionContext)) {
            throw unsupported("不支持 CTE、集合运算或嵌套根查询");
        }
        HqlParser.QuerySpecExpressionContext ordered = (HqlParser.QuerySpecExpressionContext) expression.orderedQuery(0);
        HqlParser.QueryContext query = ordered.query();
        if ((ordered.limitOffset() != null && ordered.limitOffset().getChildCount() > 0)
                || query.groupByClause() != null || query.havingClause() != null) {
            throw unsupported("不支持内嵌 LIMIT/OFFSET、分组或 HAVING，请使用 DAO 分页参数");
        }
        HqlParser.FromClauseContext from = query.fromClause();
        if (from == null || from.entityWithJoins().size() != 1) {
            throw unsupported("要求单个根实体");
        }
        HqlParser.EntityWithJoinsContext entity = from.entityWithJoins(0);
        if (!(entity.fromRoot() instanceof HqlParser.RootEntityContext)
                || !entity.crossJoin().isEmpty() || !entity.jpaCollectionJoin().isEmpty()) {
            throw unsupported("不支持派生根实体、CROSS JOIN 或 IN 集合根");
        }
        HqlParser.RootEntityContext root = (HqlParser.RootEntityContext) entity.fromRoot();
        if (root.variable() == null) {
            throw unsupported("根实体必须有别名");
        }
        String alias = variable(root.variable());
        String selection = null;
        if (query.selectClause() != null) {
            HqlParser.SelectClauseContext select = query.selectClause();
            if (select.DISTINCT() != null || select.selectionList().selection().size() != 1
                    || select.selectionList().selection(0).variable() != null) {
                throw unsupported("不支持 DISTINCT、结果别名或多个投影结果");
            }
            selection = select.selectionList().selection(0).selectExpression().getText();
        }

        Set<String> referencedAliases = new HashSet<>();
        collectAliases(query.whereClause(), referencedAliases);
        collectAliases(ordered.orderByClause(), referencedAliases);
        List<CollectionFetchQueryPlan.JoinPart> joins = new ArrayList<>();
        for (HqlParser.JoinContext join : entity.join()) {
            if (!(join.joinTarget() instanceof HqlParser.JoinPathContext)
                    || (join.joinType() != null && (join.joinType().RIGHT() != null || join.joinType().FULL() != null))) {
                throw unsupported("不支持派生 JOIN、RIGHT JOIN 或 FULL JOIN");
            }
            boolean fetched = join.FETCH() != null;
            if (fetched && join.joinRestriction() != null) {
                throw unsupported("抓取关联不能带 ON/WITH 限制");
            }
            HqlParser.JoinPathContext path = (HqlParser.JoinPathContext) join.joinTarget();
            String text = text(statement, join);
            String plain = text;
            if (fetched) {
                int start = join.FETCH().getSymbol().getStartIndex() - join.getStart().getStartIndex();
                int end = join.FETCH().getSymbol().getStopIndex() + 1 - join.getStart().getStartIndex();
                plain = text.substring(0, start) + text.substring(end);
            } else {
                collectAliases(join, referencedAliases);
            }
            joins.add(new CollectionFetchQueryPlan.JoinPart(text, plain, path.path().getText(),
                    path.variable() == null ? null : variable(path.variable()), fetched,
                    join.joinType() != null && join.joinType().LEFT() != null));
        }
        String rootFrom = statement.substring(from.getStart().getStartIndex(), root.getStop().getStopIndex() + 1);
        return new CollectionFetchQueryPlan.Parts(root.entityName().getText(), alias, rootFrom, selection,
                text(statement, query.whereClause()), text(statement, ordered.orderByClause()), joins, referencedAliases);
    }

    private static String variable(HqlParser.VariableContext variable) {
        return variable.identifier() != null ? variable.identifier().getText() : variable.nakedIdentifier().getText();
    }

    private static String text(String statement, ParserRuleContext context) {
        return context == null ? "" : statement.substring(context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1);
    }

    private static void collectAliases(ParseTree node, Set<String> aliases) {
        collectAliases(node, aliases, new HashSet<>());
    }

    private static void collectAliases(ParseTree node, Set<String> aliases, Set<String> localAliases) {
        if (node == null) {
            return;
        }
        if (node instanceof HqlParser.QueryContext) {
            localAliases = new HashSet<>(localAliases);
            HqlParser.FromClauseContext from = ((HqlParser.QueryContext) node).fromClause();
            if (from != null) {
                for (HqlParser.EntityWithJoinsContext entity : from.entityWithJoins()) {
                    if (entity.fromRoot() instanceof HqlParser.RootEntityContext) {
                        HqlParser.VariableContext alias = ((HqlParser.RootEntityContext) entity.fromRoot()).variable();
                        if (alias != null) localAliases.add(variable(alias));
                    }
                    for (HqlParser.JoinContext join : entity.join()) {
                        if (join.joinTarget() instanceof HqlParser.JoinPathContext) {
                            HqlParser.VariableContext alias = ((HqlParser.JoinPathContext) join.joinTarget()).variable();
                            if (alias != null) localAliases.add(variable(alias));
                        }
                    }
                }
            }
        }
        if (node instanceof HqlParser.SimplePathContext) {
            String alias = ((HqlParser.SimplePathContext) node).identifier().getText();
            if (!localAliases.contains(alias)) aliases.add(alias);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectAliases(node.getChild(i), aliases, localAliases);
        }
    }

    private static IllegalArgumentException unsupported(String detail) {
        return new IllegalArgumentException("集合抓取分页无法安全派生两阶段查询: " + detail);
    }
}
