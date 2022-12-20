package com.levin.commons.dao.codegen.db;


import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 将各数据库类型格式化成统一的类型
 * @see TypeEnum
 * @author tanghc
 */
public interface TypeFormatter {

    default String format(String columnType) {
        if (isBit(columnType)) {
            return TypeEnum.BIT.getType();
        }
        if (isBoolean(columnType)) {
            return TypeEnum.BOOLEAN.getType();
        }
        if (isTinyint(columnType)) {
            return TypeEnum.TINYINT.getType();
        }
        if (isSmallint(columnType)) {
            return TypeEnum.SMALLINT.getType();
        }
        if (isInt(columnType)) {
            return TypeEnum.INT.getType();
        }
        if (isLong(columnType)) {
            return TypeEnum.BIGINT.getType();
        }
        if (isFloat(columnType)) {
            return TypeEnum.FLOAT.getType();
        }
        if (isDouble(columnType)) {
            return TypeEnum.DOUBLE.getType();
        }
        if (isDecimal(columnType)) {
            return TypeEnum.DECIMAL.getType();
        }
        if(isJsonb(columnType)){
            return TypeEnum.JSONB.getType();
        }
        if (isVarchar(columnType)) {
            return TypeEnum.VARCHAR.getType();
        }
        if (isDatetime(columnType)) {
            return TypeEnum.DATETIME.getType();
        }
        if (isBlob(columnType)) {
            return TypeEnum.BLOB.getType();
        }

        return TypeEnum.VARCHAR.getType();
    }

    default boolean contains(List<String> columnTypes, String type) {
        for (String columnType : columnTypes) {
            if (StringUtils.containsIgnoreCase(type, columnType)) {
                return true;
            }
        }
        return false;
    }

    boolean isBit(String columnType);
    boolean isBoolean(String columnType);
    boolean isTinyint(String columnType);
    boolean isSmallint(String columnType);
    boolean isInt(String columnType);
    boolean isLong(String columnType);
    boolean isFloat(String columnType);
    boolean isDouble(String columnType);
    boolean isDecimal(String columnType);
    boolean isVarchar(String columnType);
    boolean isDatetime(String columnType);
    boolean isBlob(String columnType);
    boolean isJsonb(String columnType);
}
