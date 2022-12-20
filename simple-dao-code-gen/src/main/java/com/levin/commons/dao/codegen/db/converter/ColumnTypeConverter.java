package com.levin.commons.dao.codegen.db.converter;

/**
 * 将数据库类型转换成各语言对应的类型
 * @see com.levin.commons.dao.codegen.db.TypeEnum
 * @author tanghc
 */
public interface ColumnTypeConverter {

    /**
     * 将数据库类型转成基本类型
     * @param type 数据库类型
     * @return 基本类型
     */
    String convertType(String type);

    /**
     * 将数据库类型转成装箱类型
     * @param type 数据库类型
     * @return 装箱类型
     */
    String convertTypeBox(String type);

}
