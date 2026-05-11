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
     * 更新 JSON 路径的值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param paramValue     新值
     * @return this
     */
    T jsonSet(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonSet(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonSet(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonSet(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonSet(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonSet(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonSet(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
    }

    /**
     * 仅替换 JSON 中已存在路径的值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param paramValue     新值
     * @return this
     */
    T jsonReplace(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonReplace(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonReplace(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonReplace(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonReplace(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonReplace(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonReplace(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
    }

    /**
     * 仅在 JSON 路径不存在时插入值。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPath       JSON 路径
     * @param paramValue     新值
     * @return this
     */
    T jsonInsert(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonInsert(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonInsert(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonInsert(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonInsert(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonInsert(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonInsert(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
    }

    /**
     * 删除 JSON 中一个或多个明确路径。
     *
     * @param entityAttrName JSON 字段名
     * @param jsonPathList   JSON 路径列表
     * @return this
     */
    T jsonRemove(String entityAttrName, String... jsonPathList);

    default T jsonRemove(Boolean isAppend, String entityAttrName, String... jsonPathList) {
        return Boolean.TRUE.equals(isAppend) ? jsonRemove(entityAttrName, jsonPathList) : (T) this;
    }

    default T jsonRemove(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String... jsonPathList) {
        return jsonRemove(lambdaMethodAttr.getAttrName(), jsonPathList);
    }

    default T jsonRemove(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String... jsonPathList) {
        return Boolean.TRUE.equals(isAppend) ? jsonRemove(lambdaMethodAttr, jsonPathList) : (T) this;
    }

    /**
     * 按 RFC 7396 合并 JSON patch。
     *
     * @param entityAttrName JSON 字段名
     * @param patchValue     patch 文档
     * @return this
     */
    T jsonMergePatch(String entityAttrName, Object patchValue);

    default T jsonMergePatch(Boolean isAppend, String entityAttrName, Object patchValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonMergePatch(entityAttrName, patchValue) : (T) this;
    }

    default T jsonMergePatch(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object patchValue) {
        return jsonMergePatch(lambdaMethodAttr.getAttrName(), patchValue);
    }

    default T jsonMergePatch(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object patchValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonMergePatch(lambdaMethodAttr, patchValue) : (T) this;
    }

    /**
     * 向 JSON 数组根路径追加元素。
     *
     * @param entityAttrName JSON 数组字段名
     * @param paramValue     追加值
     * @return this
     */
    default T jsonArrayAppend(String entityAttrName, Object paramValue) {
        return jsonArrayAppend(entityAttrName, "$", paramValue);
    }

    default T jsonArrayAppend(Boolean isAppend, String entityAttrName, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayAppend(entityAttrName, paramValue) : (T) this;
    }

    default T jsonArrayAppend(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return jsonArrayAppend(lambdaMethodAttr.getAttrName(), paramValue);
    }

    default T jsonArrayAppend(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayAppend(lambdaMethodAttr, paramValue) : (T) this;
    }

    /**
     * 向 JSON 数组指定路径追加元素。
     *
     * @param entityAttrName JSON 数组字段名
     * @param jsonPath       JSON 数组路径
     * @param paramValue     追加值
     * @return this
     */
    T jsonArrayAppend(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonArrayAppend(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayAppend(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonArrayAppend(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonArrayAppend(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonArrayAppend(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayAppend(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
    }

    /**
     * 向 JSON 数组指定位置插入元素。
     *
     * @param entityAttrName JSON 数组字段名
     * @param jsonPath       JSON 数组位置路径
     * @param paramValue     插入值
     * @return this
     */
    T jsonArrayInsert(String entityAttrName, String jsonPath, Object paramValue);

    default T jsonArrayInsert(Boolean isAppend, String entityAttrName, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayInsert(entityAttrName, jsonPath, paramValue) : (T) this;
    }

    default T jsonArrayInsert(LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return jsonArrayInsert(lambdaMethodAttr.getAttrName(), jsonPath, paramValue);
    }

    default T jsonArrayInsert(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?> lambdaMethodAttr, String jsonPath, Object paramValue) {
        return Boolean.TRUE.equals(isAppend) ? jsonArrayInsert(lambdaMethodAttr, jsonPath, paramValue) : (T) this;
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
