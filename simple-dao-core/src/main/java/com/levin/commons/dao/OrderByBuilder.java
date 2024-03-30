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

    default T orderBy(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, null, attrReadFunctions);
    }

    /**
     * @param type
     * @param columnNames
     * @return
     */
    default T orderBy(OrderBy.Type type, String... columnNames) {
        return orderBy(true, type, columnNames);
    }

    default T orderBy(OrderBy.Type type, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, type, attrReadFunctions);
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

    default T orderBy(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(isAppend, null, attrReadFunctions);
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
     * @param attrReadFunctions
     * @return
     */
    default T orderByDesc(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, OrderBy.Type.Desc, attrReadFunctions);
    }

    /**
     * 增加降序排序
     *
     * @param scope
     * @param attrReadFunctions
     * @return
     */
    default T orderByDesc(OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, OrderBy.Type.Desc, scope, attrReadFunctions);
    }

    /**
     * @param isAppend
     * @param columnNames
     * @return
     */
    default T orderByDesc(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, OrderBy.Type.Desc, columnNames);
    }

    default T orderByDesc(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(isAppend, OrderBy.Type.Desc, attrReadFunctions);
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
     * @param attrReadFunctions
     * @return
     */
    default T orderByAsc(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(isAppend, OrderBy.Type.Asc, attrReadFunctions);
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
     * @param attrReadFunctions
     * @return
     */
    default T orderByAsc(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, OrderBy.Type.Asc, attrReadFunctions);
    }

    default T orderByAsc(OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, OrderBy.Type.Asc, scope, attrReadFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param attrReadFunctions
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(isAppend, type, null, attrReadFunctions);
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
     * @param attrReadFunctions
     * @return
     */
    default T orderBy(OrderBy.Type type, OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return orderBy(true, type, scope, attrReadFunctions);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param scope            生效的作用域
     * @param attrReadFunctions
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, OrderBy.Scope scope, PFunction<DOMAIN, ?>... attrReadFunctions) {

        //快速返回，优化性能
        return (!Boolean.TRUE.equals(isAppend))?(T) this : orderBy(isAppend, type, scope, Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
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
