package com.levin.commons.dao;

import com.levin.commons.dao.annotation.order.OrderBy;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 排序构造
 */
public interface OrderByBuilder<T extends OrderByBuilder<T, DOMAIN>, DOMAIN> {


    /**
     * 增加排序表达式，可设置参数
     *
     * @param statement   排序表达式
     * @param paramValues
     * @return
     */
    default T orderByStatement(String statement, Object... paramValues) {
        return orderByStatement(true, null, statement, paramValues);
    }

    /**
     * 增加排序表达式，可设置参数
     *
     * @param type
     * @param statement   排序表达式
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
     * @param statement   排序表达式
     * @param paramValues
     * @return
     */
    default T orderByStatement(Boolean isAppend, OrderBy.Type type, String statement, Object... paramValues) {
        return orderByStatement(isAppend, type, null, statement, paramValues);
    }

    /**
     * 增加排序表达式，可设置参数
     *
     * @param isAppend
     * @param type
     * @param scope       生效的作用域
     * @param statement   排序表达式
     * @param paramValues
     * @return
     */
    T orderByStatement(Boolean isAppend, OrderBy.Type type, OrderBy.Scope scope, String statement, Object... paramValues);


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

    default T orderByDesc(OrderBy.Scope scope, String... columnNames) {
        return orderBy(true, OrderBy.Type.Desc, scope, columnNames);
    }


    /**
     * 增加降序排序
     *
     * @param attrGetFunctions
     * @return
     */
    default T orderByDesc(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Desc, attrGetFunctions);
    }

    /**
     * 增加降序排序
     *
     * @param scope
     * @param attrGetFunctions
     * @return
     */
    default T orderByDesc(OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Desc, scope, attrGetFunctions);
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
     * 增加升序排序
     *
     * @param isAppend
     * @param columnNames
     * @return
     */
    default T orderByAsc(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, OrderBy.Type.Asc, columnNames);
    }


    /**
     * 增加升序排序
     *
     * @param attrGetFunctions
     * @return
     */
    default T orderByAsc(Boolean isAppend, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(isAppend, OrderBy.Type.Asc, attrGetFunctions);
    }

    /**
     * 增加升序排序
     *
     * @param columnNames
     * @return
     */
    default T orderByAsc(String... columnNames) {
        return orderBy(true, OrderBy.Type.Asc, columnNames);
    }

    default T orderByAsc(OrderBy.Scope scope, String... columnNames) {
        return orderBy(true, OrderBy.Type.Asc, scope, columnNames);
    }

    /**
     * 增加升序排序
     *
     * @param attrGetFunctions
     * @return
     */
    default T orderByAsc(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Asc, attrGetFunctions);
    }

    default T orderByAsc(OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, OrderBy.Type.Asc, scope, attrGetFunctions);
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
        return orderBy(isAppend, type, null, attrGetFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param type        如果不填写，默认为 Desc
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, String... columnNames) {
        return orderBy(isAppend, type, null, columnNames);
    }

    /**
     * 增加排序表达式
     *
     * @param type
     * @param scope       生效的作用域
     * @param columnNames
     * @return
     */
    default T orderBy(OrderBy.Type type, OrderBy.Scope scope, String... columnNames) {
        return orderBy(true, type, scope, columnNames);
    }

    /**
     * 增加排序表达式
     *
     * @param type
     * @param scope            生效的作用域
     * @param attrGetFunctions
     * @return
     */
    default T orderBy(OrderBy.Type type, OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrGetFunctions) {
        return orderBy(true, type, scope, attrGetFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param scope            生效的作用域
     * @param attrGetFunctions
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrGetFunctions) {

        //快速返回，优化性能
        if (!Boolean.TRUE.equals(isAppend)) {
            return (T) this;
        }

        return orderBy(isAppend, type, scope, Stream.of(attrGetFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    /**
     * 增加排序表达式，可设置参数
     *
     * @param isAppend
     * @param type
     * @param scope       生效的作用域
     * @param columnNames
     * @return
     */
    T orderBy(Boolean isAppend, OrderBy.Type type, OrderBy.Scope scope, String... columnNames);

}
