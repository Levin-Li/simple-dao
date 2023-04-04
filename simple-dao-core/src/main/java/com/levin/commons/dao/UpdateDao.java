package com.levin.commons.dao;

import lombok.Data;
import org.springframework.transaction.annotation.Propagation;
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
    UpdateDao<T> setColumns(String expr, Object... paramValues);


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
     * 增加单个需要更新的属性
     *
     * @param entityAttrName 需要更新的属性名，会自动尝试加上别名
     * @param paramValue     属性值
     * @return
     */

    UpdateDao<T> set(String entityAttrName, Object paramValue);

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
     * 使用  singleUpdate 或 batchUpdate替代
     *
     * @return 如果语句正常执行返回更新的记录数，如果语句没有执行返回 -1
     * @see #disableThrowExWhenNoColumnForUpdate
     */
    @Transactional
    @Deprecated
    int update();

    /**
     * 单条更新，并放回更新是否成功
     * <p>
     * 防止出现大量更新错误
     * <p>
     * 如果更新的记录数不是 > 1 条，将抛出异常，回滚
     *
     * @return
     */
    @Transactional
    boolean singleUpdate();

    /**
     * 批量更新，直到所有的记录都更新完成
     *
     * @param batchCommitSize
     * @return
     */
    @Transactional(propagation = Propagation.NEVER)
    int batchUpdate(int batchCommitSize);

}
