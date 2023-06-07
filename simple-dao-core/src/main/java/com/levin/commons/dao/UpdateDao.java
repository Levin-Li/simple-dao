package com.levin.commons.dao;

import org.springframework.transaction.annotation.Transactional;

public interface UpdateDao<T>
        extends ConditionBuilder<UpdateDao<T>> {

    /**
     * 增加更新的列
     *
     * @param expr        表达式
     * @param paramValues 参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
     *                    是Map时，会当成命名参数进行处理。
     * @return
     */
    default UpdateDao<T> setColumns(String expr, Object... paramValues) {
        return setColumns(true, expr, paramValues);
    }

    /**
     * 增加更新的列
     *
     * @param isAppend    是否增加，方便保持链式调用
     * @param expr        表达式
     * @param paramValues 参数值，参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理，是Map时，会当成命名参数进行处理
     * @return
     */
    UpdateDao<T> setColumns(Boolean isAppend, String expr, Object... paramValues);


    /**
     * 对字段设置NUll值
     *
     * @param entityAttrNames
     * @return
     * @since 2.3.6
     */
    UpdateDao<T> setNull(Boolean isAppend, String... entityAttrNames);

    /**
     * 对字段设置NUll值
     *
     * @param entityAttrNames
     * @return
     * @since 2.3.6
     */
    default UpdateDao<T> setNull(String... entityAttrNames) {
        return setNull(true, entityAttrNames);
    }

    /**
     * 增加单个需要更新的属性
     *
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     属性值
     * @return
     */
    default UpdateDao<T> set(String entityAttrName, Object paramValue) {
        return set(true, entityAttrName, paramValue);
    }

    /**
     * 增加单个需要更新的属性
     *
     * @param isAppend       是否增加，方便保持链式调用
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     属性值
     * @return
     */
    UpdateDao<T> set(Boolean isAppend, String entityAttrName, Object paramValue);

    /**
     * 是否有要更新的列
     * <p>
     * 2018.3.30 增加
     *
     * @return
     */
    boolean hasColumnsForUpdate();


    /**
     * 禁止抛出异常当没有要更新的列时
     *
     * @return
     */
    UpdateDao<T> disableThrowExWhenNoColumnForUpdate();


    /**
     * 执行更新动作，并返回受影响的记录数
     * <p>
     * <p>
     * <p>
     * 如果没有要更新的列该方法默认抛出异常，可以通过调用 {@link #disableThrowExWhenNoColumnForUpdate}
     * 禁止抛出异常，那将放弃执行语句返回 -1
     * <p>
     *
     * @return 如果语句正常执行返回更新的记录数，如果语句没有执行返回 -1
     * @see #disableThrowExWhenNoColumnForUpdate
     */
    @Transactional
    int update();

    /**
     * 单条更新，并放回更新是否成功
     * <p>
     * 防止出现大量更新错误
     * <p>
     * 如果更新的记录数 > 1 条，将抛出异常，回滚
     *
     * @return
     */
    @Transactional
    boolean singleUpdate();

    /**
     * 更新数据
     * <p>
     * 要求有且只有一条被更新，否则抛出异常
     */
    @Transactional
    void uniqueUpdate();
}
