package com.levin.commons.dao.codegen.db.postgresql;

import com.levin.commons.dao.codegen.db.TypeFormatter;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author tanghc
 */
public class PostgreSqlTypeFormatter implements TypeFormatter {

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
        return false;
    }

    @Override
    public boolean isSmallint(String columnType) {
        return contains(Arrays.asList("int2", "serial2", "smallint"), columnType);
    }

    @Override
    public boolean isInt(String columnType) {
        return contains(Arrays.asList("int4", "serial4", "integer"), columnType);
    }

    @Override
    public boolean isLong(String columnType) {
        return !isVarchar(columnType) && contains(Arrays.asList("int8", "serial8", "bigint"), columnType);
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
        return contains(Arrays.asList("decimal","numeric"), columnType);
    }

    @Override
    public boolean isVarchar(String columnType) {
        return contains(Arrays.asList("CHAR", "VARCHAR", "TEXT", "character", "json"), columnType);
    }

    @Override
    public boolean isDatetime(String columnType) {
        return contains(Arrays.asList("DATE", "TIME", "DATETIME", "TIMESTAMP"), columnType);
    }

    @Override
    public boolean isBlob(String columnType) {
        return contains(Collections.singletonList("blob"), columnType);
    }

    @Override
    public boolean isJsonb(String columnType) {
        return contains(Collections.singletonList("jsonb"), columnType);
    }
}
