package com.levin.commons.dao;

import org.springframework.transaction.annotation.Transactional;

public interface UpdateDao<T>
        extends ConditionBuilder<UpdateDao<T>, T>, UpdateBuilder<UpdateDao<T>, T> {

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
