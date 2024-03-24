package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleConditionBuilder<T extends SimpleConditionBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 禁用空值过滤
     * <p>
     * 默认是禁用空值过滤
     * <p>
     * 禁用后，新加入的查询条件会被强制加入
     *
     * @return
     */
    T disableEmptyValueFilter();


    /**
     * 允许空值过滤
     * <p>
     * 允许空值过滤后，新加入的条件将会过滤空值
     *
     * @return
     */
    T enableEmptyValueFilter();

//    /**
//     * is null
//     *
//     * @param entityAttrName 如 name
//     * @return
//     */
//    T isNull(String entityAttrName);

    /**
     * @param entityAttrNames
     * @return
     */
    T isNull(String... entityAttrNames);

    default T isNull(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return isNull(Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

//    /**
//     * is not null
//     *
//     * @param entityAttrName 如 name
//     * @return
//     */
//    T isNotNull(String entityAttrName);

    /**
     * @param entityAttrNames
     * @return
     */
    T isNotNull(String... entityAttrNames);

    default T isNotNull(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return isNotNull(Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    /**
     * xx is null or xx = paramValue
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNullOrEq(String entityAttrName, Object paramValue);

    default T isNullOrEq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return isNullOrEq(attrReadFunction.get(), paramValue);
    }

    /**
     * =
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T eq(String entityAttrName, Object paramValue);

    default T eq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return eq(attrReadFunction.get(), paramValue);
    }


    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T notEq(String entityAttrName, Object paramValue);

    default T notEq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return notEq(attrReadFunction.get(), paramValue);
    }

    /**
     * > 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gt(String entityAttrName, Object paramValue);

    default T gt(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return gt(attrReadFunction.get(), paramValue);
    }

    /**
     * < 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lt(String entityAttrName, Object paramValue);

    default T lt(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return lt(attrReadFunction.get(), paramValue);
    }

    /**
     * >= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gte(String entityAttrName, Object paramValue);

    default T gte(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return gte(attrReadFunction.get(), paramValue);
    }

    /**
     * <= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lte(String entityAttrName, Object paramValue);

    default T lte(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return lte(attrReadFunction.get(), paramValue);
    }

    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T between(String entityAttrName, Object... paramValues);

    default T between(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return between(attrReadFunction.get(), paramValues);
    }

    /**
     * field in (?...)
     *
     * @return
     */
    T in(String entityAttrName, Object... paramValues);

    default T in(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return in(attrReadFunction.get(), paramValues);
    }


    /**
     * field not in (?...)
     *
     * @return
     */
    T notIn(String entityAttrName, Object... paramValues);

    default T notIn(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return notIn(attrReadFunction.get(), paramValues);
    }

    /**
     * like %keyword%
     *
     * @return
     */
    T contains(String entityAttrName, String keyword);

    default T contains(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return contains(attrReadFunction.get(), keyword);
    }

    /**
     * like keyword%
     *
     * @return
     */
    T startsWith(String entityAttrName, String keyword);

    default T startsWith(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return startsWith(attrReadFunction.get(), keyword);
    }

    /**
     * like %keyword
     *
     * @return
     */
    T endsWith(String entityAttrName, String keyword);

    default T endsWith(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return endsWith(attrReadFunction.get(), keyword);
    }

    /**
     * exists 操作
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T exists(Object exprOrQueryObj, Object... paramValues);

    /**
     * exists 操作
     * <p>
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T notExists(Object exprOrQueryObj, Object... paramValues);

}
