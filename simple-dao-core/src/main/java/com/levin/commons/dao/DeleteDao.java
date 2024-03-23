package com.levin.commons.dao;


import org.springframework.transaction.annotation.Transactional;

public interface DeleteDao<T>
        extends ConditionBuilder<DeleteDao<T>, T> {

    /**
     * 执行删除动作
     *
     * @return
     */
    @Transactional
    int delete();

    /**
     * 单条删除，并放回删除是否成功
     * <p>
     * 防止出现大量删除错误
     * <p>
     * 如果删除的记录数 > 1 条，将抛出异常，回滚
     *
     * @return
     */
    @Transactional
    boolean singleDelete();

    /**
     * 删除数据
     * <p>
     * 要求有且只有一条被删除，否则抛出异常
     */
    @Transactional
    void uniqueDelete();
}
