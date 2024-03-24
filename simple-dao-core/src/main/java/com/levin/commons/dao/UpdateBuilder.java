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
     * @param attrReadFunctions
     * @return
     */
    default T setNull(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return setNull(isAppend, Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
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
     * @param attrReadFunctions
     * @return
     */
    default T setNull(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return setNull(true, attrReadFunctions);
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
     * @param attrReadFunction
     * @param paramValue
     * @return
     */
    default T set(PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return set(true, attrReadFunction.get(), paramValue);
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
     * @param attrReadFunction
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return set(isAppend, false, attrReadFunction.get(), paramValue);
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
     * @param attrReadFunction
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, boolean incrementMode, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return set(isAppend, incrementMode, true, attrReadFunction.get(), paramValue);
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
     * @param isAppend
     * @param incrementMode
     * @param autoConvertNullValueForIncrementMode
     * @param attrReadFunction
     * @param paramValue
     * @return
     */
    default T set(Boolean isAppend, boolean incrementMode, boolean autoConvertNullValueForIncrementMode, PFunction<DOMAIN, ?> attrReadFunction, Object paramValue) {
        return set(isAppend, incrementMode, autoConvertNullValueForIncrementMode, attrReadFunction.get(), paramValue);
    }

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
