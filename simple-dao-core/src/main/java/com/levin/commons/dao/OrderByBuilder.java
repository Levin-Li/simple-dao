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

    default T orderBy(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, null, lambdaMethodAttrs);
    }

    /**
     * @param type
     * @param columnNames
     * @return
     */
    default T orderBy(OrderBy.Type type, String... columnNames) {
        return orderBy(true, type, columnNames);
    }

    default T orderBy(OrderBy.Type type, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, type, lambdaMethodAttrs);
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

    default T orderBy(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(isAppend, null, lambdaMethodAttrs);
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
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderByDesc(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, OrderBy.Type.Desc, lambdaMethodAttrs);
    }

    /**
     * 增加降序排序
     *
     * @param scope
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderByDesc(OrderBy.Scope scope, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, OrderBy.Type.Desc, scope, lambdaMethodAttrs);
    }

    /**
     * @param isAppend
     * @param columnNames
     * @return
     */
    default T orderByDesc(Boolean isAppend, String... columnNames) {
        return orderBy(isAppend, OrderBy.Type.Desc, columnNames);
    }

    default T orderByDesc(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(isAppend, OrderBy.Type.Desc, lambdaMethodAttrs);
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
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderByAsc(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(isAppend, OrderBy.Type.Asc, lambdaMethodAttrs);
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
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderByAsc(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, OrderBy.Type.Asc, lambdaMethodAttrs);
    }

    default T orderByAsc(OrderBy.Scope scope, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, OrderBy.Type.Asc, scope, lambdaMethodAttrs);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(isAppend, type, null, lambdaMethodAttrs);
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
     * @param scope             生效的作用域
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderBy(OrderBy.Type type, OrderBy.Scope scope, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return orderBy(true, type, scope, lambdaMethodAttrs);
    }

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param type
     * @param scope             生效的作用域
     * @param lambdaMethodAttrs
     * @return
     */
    default T orderBy(Boolean isAppend, OrderBy.Type type, OrderBy.Scope scope, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {

        if (Boolean.TRUE.equals(isAppend)) {

            orderBy(true, type, scope,
                    Stream.of(lambdaMethodAttrs)
                            .filter(Objects::nonNull)
                            .map(LambdaMethodAttr::getAttrName)
                            .toArray(String[]::new)
            );

        }

        return (T) this;
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
