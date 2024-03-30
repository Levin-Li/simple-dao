package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;

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

    /**
     * @param entityAttrNames
     * @return
     */
    T isNull(String... entityAttrNames);

    default T isNull(Boolean isAppend, String... entityAttrNames) {
        return TRUE.equals(isAppend) ? isNull(entityAttrNames) : (T) this;
    }


    default T isNull(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return isNull(Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    default T isNull(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return TRUE.equals(isAppend) ? isNull(attrReadFunctions) : (T) this;
    }

    /**
     * @param entityAttrNames
     * @return
     */
    T isNotNull(String... entityAttrNames);

    default T isNotNull(Boolean isAppend, String... entityAttrNames) {
        return TRUE.equals(isAppend) ? isNotNull(entityAttrNames) : (T) this;
    }

    default T isNotNull(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return isNotNull(Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    default T isNotNull(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return TRUE.equals(isAppend) ? isNotNull(attrReadFunctions) : (T) this;
    }

    /**
     * xx is null or xx = paramValue
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNullOrEq(String entityAttrName, Object paramValue);

    default T isNullOrEq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? isNullOrEq(entityAttrName, paramValue) : (T) this;
    }

    default T isNullOrEq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return isNullOrEq(attrReadFunction.get(), paramValue);
    }


    default T isNullOrEq(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? isNullOrEq(attrReadFunction, paramValue) : (T) this;
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

    default T eq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? eq(entityAttrName, paramValue) : (T) this;
    }

    default T eq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return eq(attrReadFunction.get(), paramValue);
    }

    default T eq(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? eq(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T notEq(String entityAttrName, Object paramValue);

    default T notEq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? notEq(entityAttrName, paramValue) : (T) this;
    }

    default T notEq(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return notEq(attrReadFunction.get(), paramValue);
    }

    default T notEq(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? notEq(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * > 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gt(String entityAttrName, Object paramValue);

    default T gt(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? gt(entityAttrName, paramValue) : (T) this;
    }

    default T gt(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return gt(attrReadFunction.get(), paramValue);
    }

    default T gt(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? gt(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * < 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lt(String entityAttrName, Object paramValue);

    default T lt(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? lt(entityAttrName, paramValue) : (T) this;
    }

    default T lt(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return lt(attrReadFunction.get(), paramValue);
    }

    default T lt(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? lt(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * >= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gte(String entityAttrName, Object paramValue);

    default T gte(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? gte(entityAttrName, paramValue) : (T) this;
    }

    default T gte(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return gte(attrReadFunction.get(), paramValue);
    }

    default T gte(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? gte(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * <= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lte(String entityAttrName, Object paramValue);

    default T lte(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? lte(entityAttrName, paramValue) : (T) this;
    }

    default T lte(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return lte(attrReadFunction.get(), paramValue);
    }


    default T lte(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return TRUE.equals(isAppend) ? lte(attrReadFunction, paramValue) : (T) this;
    }

    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T between(String entityAttrName, Object... paramValues);

    default T between(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? between(entityAttrName, paramValues) : (T) this;
    }

    default T between(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return between(attrReadFunction.get(), paramValues);
    }

    default T between(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return TRUE.equals(isAppend) ? between(attrReadFunction, paramValues) : (T) this;
    }

    /**
     * field in (?...)
     *
     * @return
     */
    T in(String entityAttrName, Object... paramValues);

    default T in(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? in(entityAttrName, paramValues) : (T) this;
    }

    default T in(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return in(attrReadFunction.get(), paramValues);
    }


    default T in(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return TRUE.equals(isAppend) ? in(attrReadFunction, paramValues) : (T) this;
    }

    /**
     * field not in (?...)
     *
     * @return
     */
    T notIn(String entityAttrName, Object... paramValues);

    default T notIn(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? notIn(entityAttrName, paramValues) : (T) this;
    }

    default T notIn(PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return notIn(attrReadFunction.get(), paramValues);
    }

    default T notIn(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object... paramValues) {
        return TRUE.equals(isAppend) ? notIn(attrReadFunction, paramValues) : (T) this;
    }

    /**
     * like %keyword%
     *
     * @return
     */
    T contains(String entityAttrName, String keyword);


    default T contains(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? contains(entityAttrName, keyword) : (T) this;
    }

    default T contains(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return contains(attrReadFunction.get(), keyword);
    }

    default T contains(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return TRUE.equals(isAppend) ? contains(attrReadFunction, keyword) : (T) this;
    }

    /**
     * like keyword%
     *
     * @return
     */
    T startsWith(String entityAttrName, String keyword);

    default T startsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? startsWith(entityAttrName, keyword) : (T) this;
    }

    default T startsWith(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return startsWith(attrReadFunction.get(), keyword);
    }

    default T startsWith(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return TRUE.equals(isAppend) ? startsWith(attrReadFunction, keyword) : (T) this;
    }

    /**
     * like %keyword
     *
     * @return
     */
    T endsWith(String entityAttrName, String keyword);

    default T endsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? endsWith(entityAttrName, keyword) : (T) this;
    }

    default T endsWith(PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return endsWith(attrReadFunction.get(), keyword);
    }

    default T endsWith(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, String keyword) {
        return TRUE.equals(isAppend) ? endsWith(attrReadFunction, keyword) : (T) this;
    }

    /**
     * exists 操作
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T exists(Object exprOrQueryObj, Object... paramValues);

    default T exists(Boolean isAppend, Object exprOrQueryObj, Object... paramValues) {
        return TRUE.equals(isAppend) ? exists(exprOrQueryObj, paramValues) : (T) this;
    }

    /**
     * exists 操作
     * <p>
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T notExists(Object exprOrQueryObj, Object... paramValues);

    default T notExists(Boolean isAppend, Object exprOrQueryObj, Object... paramValues) {
        return TRUE.equals(isAppend) ? notExists(exprOrQueryObj, paramValues) : (T) this;
    }

}
