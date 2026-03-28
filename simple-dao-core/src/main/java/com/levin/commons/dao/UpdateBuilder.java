package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 更新语句构建
 */
public interface UpdateBuilder<T extends UpdateBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 对字段设置NUll值
     *
     * @param entityAttrNames
     * @return
     * @since 2.3.6
     */
    T setNull(Boolean isAppend, String... entityAttrNames);

    /**
     * @param isAppend
     * @param lambdaMethodAttrs
     * @return
     */
    default T setNull(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return setNull(isAppend, Stream.of(lambdaMethodAttrs).filter(Objects::nonNull).map(LambdaMethodAttr::getAttrName).toArray(String[]::new));
    }

    /**
     * 对字段设置NUll值
     *
     * @param entityAttrNames
     * @return
     * @since 2.3.6
     */
    default T setNull(String... entityAttrNames) {
        return setNull(true, entityAttrNames);
    }

    /**
     * @param lambdaMethodAttrs
     * @return
     */
    default T setNull(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return setNull(true, lambdaMethodAttrs);
    }

    ///////////////////////////////////////////////////////////////////////////////

    /**
     * 设置更新字段
     *
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     参数值
     * @return
     */
    default T set(String entityAttrName, Object paramValue) {
        return set(true, entityAttrName, paramValue);
    }

    /**
     * @param lambdaMethodAttr
     * @param paramValue
     * @return
     */
    default T set(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return set(true, lambdaMethodAttr, paramValue);
    }

    /**
     * 设置更新字段
     *
     * @param isAppend       是否加入表达式，方便链式调
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     参数值
     * @return
     */
    default T set(Boolean isAppend, String entityAttrName, Object paramValue) {
        return set(isAppend, false, entityAttrName, paramValue);
    }

    /**
     * @param isAppend
     * @param lambdaMethodAttr
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return set(isAppend, false, lambdaMethodAttr, paramValue);
    }

    /**
     * 设置更新字段
     *
     * @param isAppend       是否加入表达式，方便链式调
     * @param incrementMode  是否增量模式
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     参数值
     * @return
     */
    default T set(Boolean isAppend, boolean incrementMode, String entityAttrName, Object paramValue) {
        return set(isAppend, incrementMode, true, entityAttrName, paramValue);
    }

    /**
     * @param isAppend
     * @param incrementMode
     * @param lambdaMethodAttr
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, boolean incrementMode, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return set(isAppend, incrementMode, true, lambdaMethodAttr, paramValue);
    }

    /**
     * @param isAppend
     * @param incrementMode
     * @param autoConvertNullValueForIncrementMode
     * @param lambdaMethodAttr
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, boolean incrementMode, boolean autoConvertNullValueForIncrementMode, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return set(isAppend, incrementMode, autoConvertNullValueForIncrementMode, lambdaMethodAttr.get(), paramValue);
    }


    /**
     * 设置更新字段
     *
     * @param isAppend                             是否加入表达式，方便链式调
     * @param incrementMode                        是否增量模式
     * @param autoConvertNullValueForIncrementMode 增量模式时，是否自动转换空值
     * @param entityAttrName                       需要更新的属性名，会自动尝试加上别名
     * @param paramValue                           参数值
     * @return
     */
    T set(Boolean isAppend, boolean incrementMode, boolean autoConvertNullValueForIncrementMode, String entityAttrName, Object paramValue);

    /**
     * 增加更新表达式，可设置参数
     *
     * @param statement
     * @param paramValues
     * @return
     */
    default T setByStatement(String statement, Object... paramValues) {
        return setByStatement(true, statement, paramValues);
    }

    /**
     * 增加更新表达式，可设置参数
     *
     * @param isAppend
     * @param statement
     * @param paramValues 参数值，参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理，是Map时，会当成命名参数进行处理
     * @return
     */
    T setByStatement(Boolean isAppend, String statement, Object... paramValues);
}
