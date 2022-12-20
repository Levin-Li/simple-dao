package com.levin.commons.dao.codegen.db.table;


import com.levin.commons.dao.codegen.db.DbType;

import javax.annotation.Nullable;

/**
 * Query
 *
 * @author 阿沐 babamu@126.com
 */
public interface DBTableService {

    /**
     * 数据库类型
     */
    DbType dbType();

    /**
     * 表信息查询 SQL
     * 不传表面，则查训所有的表
     */
    String tableSql(@Nullable String tableName);

    /**
     * 表名称
     */
    String tableName();

    /**
     * 表注释
     */
    String tableComment();

    /**
     * 表字段信息查询 SQL
     */
    String tableFieldsSql();

    /**
     * 字段名称
     */
    String fieldName();

    /**
     * 字段类型
     */
    String fieldType();

    /**
     * 字段注释
     */
    String fieldComment();

    /**
     * 主键字段
     */
    String fieldKey();
}
