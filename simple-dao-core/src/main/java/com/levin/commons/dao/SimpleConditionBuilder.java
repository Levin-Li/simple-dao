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

    default T isNull(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return isNull(Stream.of(lambdaMethodAttrs).filter(Objects::nonNull).map(LambdaMethodAttr::getAttrName).toArray(String[]::new));
    }

    default T isNull(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return TRUE.equals(isAppend) ? isNull(lambdaMethodAttrs) : (T) this;
    }

    /**
     * @param entityAttrNames
     * @return
     */
    T isNotNull(String... entityAttrNames);

    default T isNotNull(Boolean isAppend, String... entityAttrNames) {
        return TRUE.equals(isAppend) ? isNotNull(entityAttrNames) : (T) this;
    }

    default T isNotNull(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return isNotNull(Stream.of(lambdaMethodAttrs).filter(Objects::nonNull).map(LambdaMethodAttr::getAttrName).toArray(String[]::new));
    }

    default T isNotNull(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return TRUE.equals(isAppend) ? isNotNull(lambdaMethodAttrs) : (T) this;
    }

    /**
     * JSON 路径存在性查询。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @return this
     */
    T jsonExists(String entityAttrName, String jsonPath);

    default T jsonExists(Boolean isAppend, String entityAttrName, String jsonPath) {
        return TRUE.equals(isAppend) ? jsonExists(entityAttrName, jsonPath) : (T) this;
    }

    default T jsonExists(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath) {
        return jsonExists(lambdaMethodAttr.getAttrName(), jsonPath);
    }

    default T jsonExists(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath) {
        return TRUE.equals(isAppend) ? jsonExists(lambdaMethodAttr, jsonPath) : (T) this;
    }

    /**
     * xx is null or xx = paramValue
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNullOrEq(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T isNullOrEq(String entityAttrName, Object paramValue) {
        return isNullOrEq((Class<?>) null, entityAttrName, paramValue);
    }

    default T isNullOrEq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? isNullOrEq(entityAttrName, paramValue) : (T) this;
    }

    default T isNullOrEq(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return isNullOrEq(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }


    default T isNullOrEq(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? isNullOrEq(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * =
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T eq(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T eq(String entityAttrName, Object paramValue) {
        return eq((Class<?>) null, entityAttrName, paramValue);
    }

    default T eq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? eq(entityAttrName, paramValue) : (T) this;
    }

    default T eq(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return eq(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T eq(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? eq(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * JSON 路径等值查询。
     *
     * @param entityAttrName JSON 字段名，如 logs
     * @param jsonPath       JSON 路径，如 $.profile.name 或 $[0].logText
     * @param paramValue     查询值
     * @return this
     */
    T jsonEq(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonEq(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return TRUE.equals(isAppend) ? jsonEq(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonEq(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonEq(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonEq(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return TRUE.equals(isAppend) ? jsonEq(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
    }

    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T notEq(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T notEq(String entityAttrName, Object paramValue) {
        return notEq((Class<?>) null, entityAttrName, paramValue);
    }

    default T notEq(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? notEq(entityAttrName, paramValue) : (T) this;
    }

    default T notEq(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return notEq(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T notEq(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? notEq(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * > 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gt(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T gt(String entityAttrName, Object paramValue) {
        return gt((Class<?>) null, entityAttrName, paramValue);
    }

    default T gt(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? gt(entityAttrName, paramValue) : (T) this;
    }

    default T gt(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return gt(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T gt(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? gt(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * < 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lt(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T lt(String entityAttrName, Object paramValue) {
        return lt((Class<?>) null, entityAttrName, paramValue);
    }

    default T lt(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? lt(entityAttrName, paramValue) : (T) this;
    }

    default T lt(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return lt(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T lt(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? lt(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * >= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gte(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T gte(String entityAttrName, Object paramValue) {
        return gte((Class<?>) null, entityAttrName, paramValue);
    }

    default T gte(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? gte(entityAttrName, paramValue) : (T) this;
    }

    default T gte(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return gte(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T gte(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? gte(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * <= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lte(Class<?> attrBelongClass, String entityAttrName, Object paramValue);

    default T lte(String entityAttrName, Object paramValue) {
        return lte((Class<?>) null, entityAttrName, paramValue);
    }

    default T lte(Boolean isAppend, String entityAttrName, Object paramValue) {
        return TRUE.equals(isAppend) ? lte(entityAttrName, paramValue) : (T) this;
    }

    default T lte(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return lte(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValue);
    }


    default T lte(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return TRUE.equals(isAppend) ? lte(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T between(Class<?> attrBelongClass, String entityAttrName, Object... paramValues);

    default T between(String entityAttrName, Object... paramValues) {
        return between((Class<?>) null, entityAttrName, paramValues);
    }

    default T between(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? between(entityAttrName, paramValues) : (T) this;
    }

    default T between(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return between(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValues);
    }

    default T between(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return TRUE.equals(isAppend) ? between(lambdaMethodAttr, paramValues) : (T) this;
    }


    /**
     * field not Between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T notBetween(Class<?> attrBelongClass, String entityAttrName, Object... paramValues);

    default T notBetween(String entityAttrName, Object... paramValues) {
        return notBetween((Class<?>) null, entityAttrName, paramValues);
    }

    default T notBetween(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? notBetween(entityAttrName, paramValues) : (T) this;
    }

    default T notBetween(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return notBetween(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValues);
    }

    default T notBetween(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return TRUE.equals(isAppend) ? notBetween(lambdaMethodAttr, paramValues) : (T) this;
    }

    /**
     * field in (?...)
     *
     * @return
     */
    T in(Class<?> attrBelongClass, String entityAttrName, Object... paramValues);

    default T in(String entityAttrName, Object... paramValues) {
        return in((Class<?>) null, entityAttrName, paramValues);
    }

    default T in(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? in(entityAttrName, paramValues) : (T) this;
    }

    default T in(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return in(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValues);
    }


    default T in(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return TRUE.equals(isAppend) ? in(lambdaMethodAttr, paramValues) : (T) this;
    }

    /**
     * field not in (?...)
     *
     * @return
     */
    T notIn(Class<?> attrBelongClass, String entityAttrName, Object... paramValues);

    default T notIn(String entityAttrName, Object... paramValues) {
        return notIn((Class<?>) null, entityAttrName, paramValues);
    }

    default T notIn(Boolean isAppend, String entityAttrName, Object... paramValues) {
        return TRUE.equals(isAppend) ? notIn(entityAttrName, paramValues) : (T) this;
    }

    default T notIn(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return notIn(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), paramValues);
    }

    default T notIn(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object... paramValues) {
        return TRUE.equals(isAppend) ? notIn(lambdaMethodAttr, paramValues) : (T) this;
    }

    /**
     * like %keyword%
     *
     * @return
     */
    T contains(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T contains(String entityAttrName, String keyword) {
        return contains((Class<?>) null, entityAttrName, keyword);
    }

    default T contains(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? contains(entityAttrName, keyword) : (T) this;
    }

    default T contains(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return contains(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T contains(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? contains(lambdaMethodAttr, keyword) : (T) this;
    }

    /**
     * JSON 路径包含查询。
     * <p>
     * 普通路径使用 json_value，通配路径（如 $[*]）使用 json_query。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param keyword        关键字
     * @return this
     */
    T jsonContains(String entityAttrName, String jsonPath, String keyword);

    default T jsonContains(Boolean isAppend, String entityAttrName, String jsonPath, String keyword) {
        return TRUE.equals(isAppend) ? jsonContains(entityAttrName, jsonPath, keyword) : (T) this;
    }

    default T jsonContains(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, String keyword) {
        return jsonContains(lambdaMethodAttr.getAttrName(), jsonPath, keyword);
    }

    default T jsonContains(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, String keyword) {
        return TRUE.equals(isAppend) ? jsonContains(lambdaMethodAttr, jsonPath, keyword) : (T) this;
    }

    T notContains(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T notContains(String entityAttrName, String keyword) {
        return notContains((Class<?>) null, entityAttrName, keyword);
    }

    default T notContains(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? notContains(entityAttrName, keyword) : (T) this;
    }

    default T notContains(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return notContains(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T notContains(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? notContains(lambdaMethodAttr, keyword) : (T) this;
    }

    /**
     * like keyword%
     *
     * @return
     */
    T startsWith(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T startsWith(String entityAttrName, String keyword) {
        return startsWith((Class<?>) null, entityAttrName, keyword);
    }

    default T startsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? startsWith(entityAttrName, keyword) : (T) this;
    }

    default T startsWith(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return startsWith(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T startsWith(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? startsWith(lambdaMethodAttr, keyword) : (T) this;
    }

    /**
     * like keyword%
     *
     * @return
     */
    T notStartsWith(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T notStartsWith(String entityAttrName, String keyword) {
        return notStartsWith((Class<?>) null, entityAttrName, keyword);
    }

    default T notStartsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? notStartsWith(entityAttrName, keyword) : (T) this;
    }

    default T notStartsWith(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return notStartsWith(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T notStartsWith(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? notStartsWith(lambdaMethodAttr, keyword) : (T) this;
    }

    /**
     * like %keyword
     *
     * @return
     */
    T endsWith(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T endsWith(String entityAttrName, String keyword) {
        return endsWith((Class<?>) null, entityAttrName, keyword);
    }

    default T endsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? endsWith(entityAttrName, keyword) : (T) this;
    }

    default T endsWith(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return endsWith(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T endsWith(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? endsWith(lambdaMethodAttr, keyword) : (T) this;
    }

    /**
     * like %keyword
     *
     * @return
     */
    T notEndsWith(Class<?> attrBelongClass, String entityAttrName, String keyword);

    default T notEndsWith(String entityAttrName, String keyword) {
        return notEndsWith((Class<?>) null, entityAttrName, keyword);
    }

    default T notEndsWith(Boolean isAppend, String entityAttrName, String keyword) {
        return TRUE.equals(isAppend) ? notEndsWith(entityAttrName, keyword) : (T) this;
    }

    default T notEndsWith(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return notEndsWith(lambdaMethodAttr.getAttrClass(), lambdaMethodAttr.getAttrName(), keyword);
    }

    default T notEndsWith(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String keyword) {
        return TRUE.equals(isAppend) ? notEndsWith(lambdaMethodAttr, keyword) : (T) this;
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
