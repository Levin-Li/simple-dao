package com.levin.commons.dao.util;

import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * SQL 解析工具类
 * <p>
 * https://github.com/alibaba/druid/wiki/Druid_SQL_AST
 */
public class QLUtils {

    private static final ConcurrentReferenceHashMap<String, List<String[]>> softRefCache = new ConcurrentReferenceHashMap();

    /**
     * 只解析 select
     *
     * @param column
     * @return
     */
    public static String parseSelectColumn(String column) {

        column = replace(column);

        List<String[]> selectColumns = parseSelectColumns(null, column);

        return (selectColumns.size() > 0) ? selectColumns.get(0)[0] : column;

    }


    /**
     * 替换特殊字符
     *
     * @param column
     * @return
     */
    private static String replace(String column) {
        return column.replace("?", "_P_")
                .replace(":", "_").trim();
    }

    /**
     * 返回数组，第一个元素时语句，第二个时别名
     *
     * @param columns
     * @return
     */
    public static List<String[]> parseSelectColumns(String dbType, String... columns) {

        StringBuffer sb = new StringBuffer();

        for (String column : columns) {

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(column);
        }

        if (sb.length() < 1) {
            return Collections.emptyList();
        }

        String cols = replace(sb.toString().trim());

        //使用缓存
        List<String[]> result = softRefCache.get(cols);

        if (result != null) {
            return result;
        }

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser("select " + cols + " from Test_Table", dbType);

        List<SQLSelectItem> selectItems = ((SQLSelectQueryBlock) ((SQLSelectStatement) parser.parseSelect()).getSelect().getQuery()).getSelectList();

        result = new ArrayList<>(selectItems.size());

        for (SQLSelectItem selectItem : selectItems) {
            result.add(new String[]{selectItem.getExpr().toString(), selectItem.getAlias()});
        }

        //放入缓存
        softRefCache.put(cols, result);

        return result;
    }


    /**
     * 替换占位符
     *
     * @param sql
     * @return
     */
    public static String replaceParamPlaceholder(String sql, String oldParamPlaceholder, Integer paramIndex, String newPlaceholderPrefix, String newPlaceholderSuffix) {

        oldParamPlaceholder = oldParamPlaceholder.trim();

        StringBuilder sb = new StringBuilder();


        int beginIndex = 0;

        int endIndex = 0;


        int len = oldParamPlaceholder.length();

        while ((beginIndex = sql.indexOf(oldParamPlaceholder, endIndex)) != -1) {

            sb.append(sql.substring(endIndex, beginIndex))
                    .append(" ")
                    .append(newPlaceholderPrefix != null ? newPlaceholderPrefix : "")
                    .append(paramIndex != null ? paramIndex++ : "")
                    .append(newPlaceholderSuffix != null ? newPlaceholderSuffix : "")
                    .append(" ");

            endIndex = beginIndex + len;
        }

        if (endIndex < sql.length()) {
            sb.append(sql.substring(endIndex));
        }

        return sb.toString();

    }

    public static void main(String[] args) {

        parseSelectColumns(null, "avg( t.aa +${:a} ) as ts");
        parseSelectColumns(null, "avg( t.aa +${a} ) as ts");
        parseSelectColumns(null, "avg( t.aa + :name) as ts");
        parseSelectColumns(null, "avg( t.aa + :name) as ts");
        parseSelectColumns(null, "avg( t.aa + :?) as ts");
        parseSelectColumns(null, "avg( t.aa + ?) as ts");
        parseSelectColumns(null, "avg( t.aa + ?1) as ts");

    }

}
