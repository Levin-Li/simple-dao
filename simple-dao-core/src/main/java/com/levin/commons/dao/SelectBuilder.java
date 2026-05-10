package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 选择语句构建
 */
public interface SelectBuilder<T extends SelectBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加选择字段
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    default T select(String... columnNames) {
        return select(true, columnNames);
    }

    default <R> T select(LambdaMethodAttr<DOMAIN, R>... lambdaMethodAttrs) {
        return select(

                Stream.of(lambdaMethodAttrs)
                        .filter(Objects::nonNull)
                        .map(LambdaMethodAttr::getAttrName)
                        .toArray(String[]::new)
        );
    }

    /**
     * 增加选择字段
     *
     * @param isAppend
     * @param lambdaMethodAttrs
     * @return
     */
    default <R>  T select(Boolean isAppend, LambdaMethodAttr<DOMAIN, R>... lambdaMethodAttrs) {
        return Boolean.TRUE.equals(isAppend) ? select(lambdaMethodAttrs) : (T) this;
    }

    /**
     * 增加选择字段
     * <p>
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    T select(Boolean isAppend, String... columnNames);

    /**
     * 选择 JSON 路径的值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param alias          结果别名
     * @return this
     */
    T jsonSelect(String entityAttrName, String jsonPath, String alias);

    default T jsonSelect(Boolean isAppend, String entityAttrName, String jsonPath, String alias) {
        return Boolean.TRUE.equals(isAppend) ? jsonSelect(entityAttrName, jsonPath, alias) : (T) this;
    }

    default <R> T jsonSelect(LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias) {
        return jsonSelect(lambdaMethodAttr.getAttrName(), jsonPath, alias);
    }

    default <R> T jsonSelect(Boolean isAppend, LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias) {
        return Boolean.TRUE.equals(isAppend) ? jsonSelect(lambdaMethodAttr, jsonPath, alias) : (T) this;
    }


    /**
     * 增加选择表达式，可设置参数
     *
     * @param statement
     * @param paramValues
     * @return
     */
    default T selectByStatement(String statement, Object... paramValues) {
        return selectByStatement(true, statement, paramValues);
    }

    /**
     * 增加选择表达式，可设置参数
     *
     * @param isAppend
     * @param statement
     * @param paramValues
     * @return
     */
    T selectByStatement(Boolean isAppend, String statement, Object... paramValues);

}
