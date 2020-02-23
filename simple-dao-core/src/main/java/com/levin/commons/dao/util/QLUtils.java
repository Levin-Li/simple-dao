package com.levin.commons.dao.util;

import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import java.util.ArrayList;
import java.util.List;

public class QLUtils {

    /**
     * @param column
     * @return
     */
    public static String parseSelectColumn(String column) {

        List<String[]> selectColumns = parseSelectColumns(null, column);

        return (selectColumns.size() > 0) ? selectColumns.get(0)[0] : column;

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

        if (sb.length() < 1)
            return new ArrayList<>(0);

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser("select " + sb.toString() + " from Test_Table", dbType);

        List<SQLSelectItem> selectItems = ((SQLSelectQueryBlock) ((SQLSelectStatement) parser.parseSelect()).getSelect().getQuery()).getSelectList();

        List<String[]> result = new ArrayList<>(selectItems.size());

        for (SQLSelectItem selectItem : selectItems) {
            result.add(new String[]{selectItem.getExpr().toString(), selectItem.getAlias()});
        }

        return result;
    }


}
