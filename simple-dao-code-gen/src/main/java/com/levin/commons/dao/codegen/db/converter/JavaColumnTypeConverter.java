package com.levin.commons.dao.codegen.db.converter;

import com.levin.commons.dao.codegen.db.TypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanghc
 */
public class JavaColumnTypeConverter implements ColumnTypeConverter {

    private static final Map<String, String> TYPE_MAP = new HashMap<>(64);
    private static final Map<String, String> TYPE_BOX_MAP = new HashMap<>(64);
    static {
        TYPE_MAP.put(TypeEnum.BIT.getType(), "boolean");
        TYPE_MAP.put(TypeEnum.BOOLEAN.getType(), "boolean");
        TYPE_MAP.put(TypeEnum.TINYINT.getType(), "byte");
        TYPE_MAP.put(TypeEnum.SMALLINT.getType(), "short");
        TYPE_MAP.put(TypeEnum.INT.getType(), "int");
        TYPE_MAP.put(TypeEnum.BIGINT.getType(), "long");
        TYPE_MAP.put(TypeEnum.FLOAT.getType(), "float");
        TYPE_MAP.put(TypeEnum.DOUBLE.getType(), "double");
        TYPE_MAP.put(TypeEnum.DECIMAL.getType(), "BigDecimal");
        TYPE_MAP.put(TypeEnum.VARCHAR.getType(), "string");
        TYPE_MAP.put(TypeEnum.DATETIME.getType(), "Date");
        TYPE_MAP.put(TypeEnum.BLOB.getType(), "byte[]");
        TYPE_MAP.put(TypeEnum.JSONB.getType(), "Map<String, Object>");

        TYPE_BOX_MAP.put(TypeEnum.BIT.getType(), "Boolean");
        TYPE_BOX_MAP.put(TypeEnum.BOOLEAN.getType(), "Boolean");
        TYPE_BOX_MAP.put(TypeEnum.TINYINT.getType(), "Byte");
        TYPE_BOX_MAP.put(TypeEnum.SMALLINT.getType(), "Short");
        TYPE_BOX_MAP.put(TypeEnum.INT.getType(), "Integer");
        TYPE_BOX_MAP.put(TypeEnum.BIGINT.getType(), "Long");
        TYPE_BOX_MAP.put(TypeEnum.FLOAT.getType(), "Float");
        TYPE_BOX_MAP.put(TypeEnum.DOUBLE.getType(), "Double");
        TYPE_BOX_MAP.put(TypeEnum.DECIMAL.getType(), "BigDecimal");
        TYPE_BOX_MAP.put(TypeEnum.VARCHAR.getType(), "String");
        TYPE_BOX_MAP.put(TypeEnum.DATETIME.getType(), "Date");
        TYPE_BOX_MAP.put(TypeEnum.BLOB.getType(), "Byte[]");
        TYPE_BOX_MAP.put(TypeEnum.JSONB.getType(), "Map<String, Object>");
    }

    @Override
    public String convertType(String type) {
        String string = TYPE_MAP.getOrDefault(type, "String");
        return string;
    }

    @Override
    public String convertTypeBox(String type) {
        return TYPE_BOX_MAP.getOrDefault(type, "String");
    }
}
