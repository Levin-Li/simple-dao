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
     * 使用 json_value 选择 JSON 标量路径的值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param alias          结果别名
     * @param clauses        Hibernate JSON 函数可选子句，如 returning、on empty、on error
     * @return this
     */
    T jsonValueSelect(String entityAttrName, String jsonPath, String alias, String... clauses);

    default T jsonValueSelect(Boolean isAppend, String entityAttrName, String jsonPath, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonValueSelect(entityAttrName, jsonPath, alias, clauses) : (T) this;
    }

    default <R> T jsonValueSelect(LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias, String... clauses) {
        return jsonValueSelect(lambdaMethodAttr.getAttrName(), jsonPath, alias, clauses);
    }

    default <R> T jsonValueSelect(Boolean isAppend, LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonValueSelect(lambdaMethodAttr, jsonPath, alias, clauses) : (T) this;
    }

    /**
     * 使用 json_query 选择 JSON 对象或数组路径的值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param alias          结果别名
     * @param clauses        Hibernate JSON 函数可选子句，如 with wrapper、on empty、on error
     * @return this
     */
    T jsonQuerySelect(String entityAttrName, String jsonPath, String alias, String... clauses);

    default T jsonQuerySelect(Boolean isAppend, String entityAttrName, String jsonPath, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonQuerySelect(entityAttrName, jsonPath, alias, clauses) : (T) this;
    }

    default <R> T jsonQuerySelect(LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias, String... clauses) {
        return jsonQuerySelect(lambdaMethodAttr.getAttrName(), jsonPath, alias, clauses);
    }

    default <R> T jsonQuerySelect(Boolean isAppend, LambdaMethodAttr<DOMAIN, R> lambdaMethodAttr, String jsonPath, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonQuerySelect(lambdaMethodAttr, jsonPath, alias, clauses) : (T) this;
    }

    /**
     * 选择 json_object(...) 构造出的 JSON 对象。
     *
     * @param alias         结果别名
     * @param entryExprList json_object 键值片段，如 "'name' value u.name"
     * @return this
     */
    T jsonObjectSelect(String alias, String... entryExprList);

    default T jsonObjectSelect(Boolean isAppend, String alias, String... entryExprList) {
        return Boolean.TRUE.equals(isAppend) ? jsonObjectSelect(alias, entryExprList) : (T) this;
    }

    /**
     * 选择 json_array(...) 构造出的 JSON 数组。
     *
     * @param alias         结果别名
     * @param valueExprList json_array 值表达式列表
     * @return this
     */
    T jsonArraySelect(String alias, String... valueExprList);

    default T jsonArraySelect(Boolean isAppend, String alias, String... valueExprList) {
        return Boolean.TRUE.equals(isAppend) ? jsonArraySelect(alias, valueExprList) : (T) this;
    }

    /**
     * 选择 json_arrayagg(...) 聚合出的 JSON 数组。
     *
     * @param valueExpr 聚合值表达式
     * @param alias     结果别名
     * @param clauses   可选子句，如 order by
     * @return this
     */
    T jsonArrayAggSelect(String valueExpr, String alias, String... clauses);

    default T jsonArrayAggSelect(Boolean isAppend, String valueExpr, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayAggSelect(valueExpr, alias, clauses) : (T) this;
    }

    /**
     * 选择 json_objectagg(...) 聚合出的 JSON 对象。
     *
     * @param keyExpr   聚合 key 表达式
     * @param valueExpr 聚合 value 表达式
     * @param alias     结果别名
     * @param clauses   可选子句，如 filter
     * @return this
     */
    T jsonObjectAggSelect(String keyExpr, String valueExpr, String alias, String... clauses);

    default T jsonObjectAggSelect(Boolean isAppend, String keyExpr, String valueExpr, String alias, String... clauses) {
        return Boolean.TRUE.equals(isAppend) ? jsonObjectAggSelect(keyExpr, valueExpr, alias, clauses) : (T) this;
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
