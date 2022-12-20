package com.levin.commons.dao.codegen.db.converter;

import com.levin.commons.dao.codegen.db.TypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanghc
 */
public class CsharpColumnTypeConverter implements ColumnTypeConverter {

    private static final Map<String, String> TYPE_MAP = new HashMap<>(64);
    static {
        TYPE_MAP.put(TypeEnum.BIT.getType(), "bool");
        TYPE_MAP.put(TypeEnum.BOOLEAN.getType(), "bool");
        TYPE_MAP.put(TypeEnum.TINYINT.getType(), "byte");
        TYPE_MAP.put(TypeEnum.SMALLINT.getType(), "int");
        TYPE_MAP.put(TypeEnum.INT.getType(), "int");
        TYPE_MAP.put(TypeEnum.BIGINT.getType(), "long");
        TYPE_MAP.put(TypeEnum.FLOAT.getType(), "float");
        TYPE_MAP.put(TypeEnum.DOUBLE.getType(), "double");
        TYPE_MAP.put(TypeEnum.DECIMAL.getType(), "decimal");
        TYPE_MAP.put(TypeEnum.VARCHAR.getType(), "string");
        TYPE_MAP.put(TypeEnum.DATETIME.getType(), "DateTime");
        TYPE_MAP.put(TypeEnum.BLOB.getType(), "byte[]");
    }

    @Override
    public String convertType(String type) {
        return TYPE_MAP.getOrDefault(type, "string");
    }

    @Override
    public String convertTypeBox(String type) {
        return this.convertType(type);
    }

}
