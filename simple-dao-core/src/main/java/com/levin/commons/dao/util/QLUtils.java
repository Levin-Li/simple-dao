package com.levin.commons.dao.util;

import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.ArrayList;
import java.util.List;

public class QLUtils {


    private static ConcurrentReferenceHashMap<String, List<String[]>> softRefCache = new ConcurrentReferenceHashMap();

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


        String cols = sb.toString().trim();

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


}
