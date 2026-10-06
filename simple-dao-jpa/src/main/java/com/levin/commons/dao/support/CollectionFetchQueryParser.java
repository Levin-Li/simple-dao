package com.levin.commons.dao.support;

import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.tree.Node;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 使用 Hibernate 的 HQL 语法树定位原文，避免自行解析 JPQL。 */
final class CollectionFetchQueryParser {

    private CollectionFetchQueryParser() {
    }

    static CollectionFetchQueryPlan.Parts parse(String statement) {
        AST query;
        try {
            HqlParser parser = HqlParser.getInstance(statement);
            parser.statement();
            parser.getParseErrorHandler().throwQueryException();
            query = parser.getAST();
        } catch (Exception ex) {
            throw new IllegalArgumentException("集合抓取分页无法解析 HQL：" + ex.getMessage(), ex);
        }
        if (query == null || query.getType() != HqlTokenTypes.QUERY || query.getNextSibling() != null) {
            throw unsupported("仅支持单个根实体 SELECT 查询");
        }

        AST selectFrom = child(query, HqlTokenTypes.SELECT_FROM);
        AST from = child(selectFrom, HqlTokenTypes.FROM);
        AST select = child(selectFrom, HqlTokenTypes.SELECT);
        AST where = child(query, HqlTokenTypes.WHERE);
        AST order = child(query, HqlTokenTypes.ORDER);
        for (AST clause = query.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
            if (clause.getType() != HqlTokenTypes.SELECT_FROM
                    && clause.getType() != HqlTokenTypes.WHERE && clause.getType() != HqlTokenTypes.ORDER) {
                throw unsupported("不支持 GROUP BY、HAVING 或其他复杂查询子句");
            }
        }
        if (from == null) {
            throw unsupported("缺少根实体 FROM 子句");
        }

        AST range = null;
        List<AST> joinNodes = new ArrayList<>();
        for (AST item = from.getFirstChild(); item != null; item = item.getNextSibling()) {
            if (item.getType() == HqlTokenTypes.RANGE) {
                if (range != null) {
                    throw unsupported("不支持多个根实体");
                }
                range = item;
            } else if (item.getType() == HqlTokenTypes.JOIN) {
                joinNodes.add(item);
            } else {
                throw unsupported("不支持此 FROM 查询形态");
            }
        }
        if (range == null) {
            throw unsupported("缺少根实体");
        }
        AST entity = range.getFirstChild();
        AST rootAliasNode = entity == null ? null : entity.getNextSibling();
        if (rootAliasNode == null || rootAliasNode.getType() != HqlTokenTypes.ALIAS
                || rootAliasNode.getNextSibling() != null) {
            throw unsupported("根实体必须指定单一别名");
        }
        String rootEntity = path(entity);
        String rootAlias = rootAliasNode.getText();
        String selectExpression = null;
        if (select != null) {
            AST expression = select.getFirstChild();
            if (expression != null && expression.getType() == HqlTokenTypes.DISTINCT) {
                throw unsupported("不支持 DISTINCT，分页保持原查询不去重语义");
            }
            if (expression == null || expression.getNextSibling() != null
                    || expression.getType() != HqlTokenTypes.IDENT || !rootAlias.equals(expression.getText())) {
                throw unsupported("SELECT 必须仅返回根实体别名");
            }
            selectExpression = rootAlias;
        }

        int fromEnd = firstOffset(statement, statement.length(), where, order);
        if (select != null && offset(statement, select) > offset(statement, from)) {
            throw unsupported("不支持 FROM 后置 SELECT");
        }
        int rootEnd = joinNodes.isEmpty() ? fromEnd : joinStart(statement, joinNodes.get(0));
        String rootFrom = statement.substring(offset(statement, from), rootEnd).trim();
        List<CollectionFetchQueryPlan.JoinPart> joins = new ArrayList<>();
        Set<String> referencedAliases = new LinkedHashSet<>();
        collectAliases(where, referencedAliases);
        collectAliases(order, referencedAliases);
        for (int i = 0; i < joinNodes.size(); i++) {
            AST join = joinNodes.get(i);
            AST fetch = child(join, HqlTokenTypes.FETCH);
            AST joinPath = child(join, HqlTokenTypes.DOT);
            AST alias = child(join, HqlTokenTypes.ALIAS);
            if (joinPath == null || child(join, HqlTokenTypes.RIGHT) != null
                    || child(join, HqlTokenTypes.FULL) != null) {
                throw unsupported("仅支持关联路径的 INNER 或 LEFT JOIN");
            }
            if (fetch != null && (child(join, HqlTokenTypes.WITH) != null
                    || child(join, HqlTokenTypes.ON) != null)) {
                throw unsupported("JOIN FETCH 不支持附加 ON 或 WITH 条件");
            }
            int start = joinStart(statement, join);
            int end = i + 1 == joinNodes.size() ? fromEnd : joinStart(statement, joinNodes.get(i + 1));
            String text = statement.substring(start, end).trim();
            String plainText = text;
            if (fetch != null) {
                int fetchStart = offset(statement, fetch);
                int fetchEnd = fetchStart + ((Node) fetch).getTextLength();
                plainText = (statement.substring(start, fetchStart) + statement.substring(fetchEnd, end)).trim();
            } else {
                collectAliases(join, referencedAliases);
            }
            joins.add(new CollectionFetchQueryPlan.JoinPart(text, plainText, path(joinPath),
                    alias == null ? null : alias.getText(), fetch != null, child(join, HqlTokenTypes.LEFT) != null));
        }
        String whereClause = where == null ? "" : statement.substring(offset(statement, where),
                firstOffset(statement, statement.length(), order)).trim();
        String orderClause = order == null ? "" : statement.substring(offset(statement, order)).trim();
        return new CollectionFetchQueryPlan.Parts(rootEntity, rootAlias, rootFrom, selectExpression,
                whereClause, orderClause, joins, referencedAliases);
    }

    private static AST child(AST parent, int type) {
        if (parent != null) {
            for (AST item = parent.getFirstChild(); item != null; item = item.getNextSibling()) {
                if (item.getType() == type) {
                    return item;
                }
            }
        }
        return null;
    }

    private static String path(AST node) {
        if (node != null && node.getType() == HqlTokenTypes.DOT) {
            AST first = node.getFirstChild();
            AST second = first == null ? null : first.getNextSibling();
            if (first == null || second == null || second.getNextSibling() != null) {
                throw unsupported("关联路径结构不受支持");
            }
            return path(first) + "." + path(second);
        }
        if (node == null || (node.getType() != HqlTokenTypes.IDENT && node.getType() != HqlTokenTypes.WEIRD_IDENT)) {
            throw unsupported("实体名或关联路径结构不受支持");
        }
        return node.getText();
    }

    private static void collectAliases(AST node, Set<String> aliases) {
        collectAliases(node, aliases, new LinkedHashSet<>());
    }

    private static void collectAliases(AST node, Set<String> aliases, Set<String> localAliases) {
        if (node == null) {
            return;
        }
        if (node.getType() == HqlTokenTypes.QUERY) {
            localAliases = new LinkedHashSet<>(localAliases);
            AST from = child(child(node, HqlTokenTypes.SELECT_FROM), HqlTokenTypes.FROM);
            if (from != null) {
                for (AST item = from.getFirstChild(); item != null; item = item.getNextSibling()) {
                    AST alias = child(item, HqlTokenTypes.ALIAS);
                    if (alias != null) localAliases.add(alias.getText());
                }
            }
        }
        if (node.getType() == HqlTokenTypes.IDENT || node.getType() == HqlTokenTypes.WEIRD_IDENT) {
            if (!localAliases.contains(node.getText())) aliases.add(node.getText());
        }
        if (node.getType() == HqlTokenTypes.DOT) {
            AST first = node;
            while (first.getType() == HqlTokenTypes.DOT && first.getFirstChild() != null) {
                first = first.getFirstChild();
            }
            if (first.getType() == HqlTokenTypes.IDENT || first.getType() == HqlTokenTypes.WEIRD_IDENT) {
                if (!localAliases.contains(first.getText())) aliases.add(first.getText());
            }
        }
        for (AST item = node.getFirstChild(); item != null; item = item.getNextSibling()) {
            collectAliases(item, aliases, localAliases);
        }
    }

    private static int joinStart(String source, AST join) {
        int start = offset(source, join);
        for (AST item = join.getFirstChild(); item != null; item = item.getNextSibling()) {
            if (item.getType() == HqlTokenTypes.LEFT || item.getType() == HqlTokenTypes.INNER
                    || item.getType() == HqlTokenTypes.RIGHT || item.getType() == HqlTokenTypes.FULL) {
                start = Math.min(start, offset(source, item));
            }
        }
        return start;
    }

    private static int firstOffset(String source, int fallback, AST... nodes) {
        int result = fallback;
        for (AST node : nodes) {
            if (node != null) {
                result = Math.min(result, offset(source, node));
            }
        }
        return result;
    }

    /** ANTLR 2 使用从 1 开始的行列，并将制表符展开到每 8 列的制表位。 */
    private static int offset(String source, AST ast) {
        Node node = (Node) ast;
        int line = 1;
        int column = 1;
        for (int i = 0; i < source.length(); i++) {
            if (line == node.getLine() && column == node.getColumn()) {
                return i;
            }
            char ch = source.charAt(i);
            if (ch == '\r') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '\n') {
                    i++;
                }
                line++;
                column = 1;
            } else if (ch == '\n') {
                line++;
                column = 1;
            } else if (ch == '\t') {
                column = ((column - 1) / 8 + 1) * 8 + 1;
            } else {
                column++;
            }
        }
        throw unsupported("无法定位 HQL 语法节点原文");
    }

    private static IllegalArgumentException unsupported(String reason) {
        return new IllegalArgumentException("集合抓取分页：" + reason);
    }
}
