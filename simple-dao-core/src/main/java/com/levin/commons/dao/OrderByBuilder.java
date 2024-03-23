package com.levin.commons.dao;

import com.levin.commons.dao.annotation.order.OrderBy;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 排序构造
 */
public interface OrderByBuilder<T extends OrderByBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加排序字段
     *
     * @param columnNames 例：  "name desc" , "createTime desc"
     * @return
     */
    default T orderBy(String... columnNames) {
        return orderBy(true, null, columnNames);
    }

    default T orderBy(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, null, attrGetFunctions);
    }

    /**
     * @param type
     * @param columnNames
     * @return
     */
    default T orderBy(OrderBy.Type type, String... columnNames) {
        return orderBy(true, type, columnNames);
    }

    default T orderBy(OrderBy.Type type, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, type, attrGetFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param columnNames 例：  "name desc" , "createTime desc"
     * @return
     */
    default T orderBy(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, null, columnNames);
    }

    default T orderBy(Boolean isAppend, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(isAppend, null, attrGetFunctions);
    }

    /**
     * @param columnNames
     * @return
     */
    default T orderByDesc(String... columnNames) {
        return orderBy(true, OrderBy.Type.Desc, columnNames);
    }

    default T orderByDesc(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Desc, attrGetFunctions);
    }

    /**
     * @param isAppend
     * @param columnNames
     * @return
     */
    default T orderByDesc(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, OrderBy.Type.Desc, columnNames);
    }

    default T orderByDesc(Boolean isAppend, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(isAppend, OrderBy.Type.Desc, attrGetFunctions);
    }

    /**
     * @param isAppend
     * @param columnNames
     * @return
     */
    default T orderByAsc(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, OrderBy.Type.Asc, columnNames);
    }

    default T orderByAsc(Boolean isAppend, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(isAppend, OrderBy.Type.Asc, attrGetFunctions);
    }

    /**
     * @param columnNames
     * @return
     */
    default T orderByAsc(String... columnNames) {
        return orderBy(true, OrderBy.Type.Asc, columnNames);
    }

    default T orderByAsc(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Asc, attrGetFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param attrGetFunctions
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, PFunction<DOMAIN, ?>... attrGetFunctions) {

        //快速返回，优化性能
        if (!Boolean.TRUE.equals(isAppend)) {
            return (T) this;
        }

        return orderBy(isAppend, type, (String[]) Stream.of(attrGetFunctions).filter(Objects::nonNull).map(f -> f.get()).toArray());
    }

    /**
     * 增加排序字段
     *
     * @param type        如果不填写，默认为 Desc
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    T orderBy(Boolean isAppend, OrderBy.Type type, String... columnNames);


    /**
     * 增加排序表达式，可设置参数
     *
     * @param type
     * @param statement
     * @param paramValues
     * @return
     */
    default T orderByStatement(OrderBy.Type type, String statement, Object... paramValues) {
        return orderByStatement(true, type, statement, paramValues);
    }

    /**
     * 增加排序表达式，可设置参数
     *
     * @param isAppend
     * @param type
     * @param statement
     * @param paramValues
     * @return
     */
    T orderByStatement(Boolean isAppend, OrderBy.Type type, String statement, Object... paramValues);
}
