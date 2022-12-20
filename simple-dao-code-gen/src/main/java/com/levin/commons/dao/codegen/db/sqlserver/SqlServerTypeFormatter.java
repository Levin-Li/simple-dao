package com.levin.commons.dao.codegen.db.sqlserver;

import com.levin.commons.dao.codegen.db.TypeFormatter;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author tanghc
 */
public class SqlServerTypeFormatter implements TypeFormatter {

    @Override
    public boolean isBit(String columnType) {
        return contains(Collections.singletonList("bit"), columnType);
    }

    @Override
    public boolean isBoolean(String columnType) {
        return contains(Collections.singletonList("boolean"), columnType);
    }

    @Override
    public boolean isTinyint(String columnType) {
        return contains(Collections.singletonList("tinyint"), columnType);
    }

    @Override
    public boolean isSmallint(String columnType) {
        return contains(Collections.singletonList("smallint"), columnType);
    }

    @Override
    public boolean isInt(String columnType) {
        return !isLong(columnType) && contains(Arrays.asList("int", "integer"), columnType);
    }

    @Override
    public boolean isLong(String columnType) {
        return !isVarchar(columnType) && contains(Collections.singletonList("bigint"), columnType);
    }

    @Override
    public boolean isFloat(String columnType) {
        return contains(Arrays.asList("float", "real"), columnType);
    }

    @Override
    public boolean isDouble(String columnType) {
        return contains(Collections.singletonList("double"), columnType);
    }

    @Override
    public boolean isDecimal(String columnType) {
        return contains(Arrays.asList("decimal", "numeric", "money", "smallmoney"), columnType);
    }

    @Override
    public boolean isVarchar(String columnType) {
        return contains(Arrays.asList("CHAR", "VARCHAR", "TEXT", "nchar", "nvarchar", "ntext"), columnType);
    }

    @Override
    public boolean isDatetime(String columnType) {
        return contains(Arrays.asList("DATE", "TIME", "DATETIME", "TIMESTAMP", "datetime2", "smalldatetime", "datetimeoffset"), columnType);
    }

    @Override
    public boolean isBlob(String columnType) {
        return contains(Arrays.asList("blob", "binary", "varbinary"), columnType);
    }

    @Override
    public boolean isJsonb(String columnType) {
        return false;
    }
}
