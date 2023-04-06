package com.levin.commons.dao;


import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface DeleteDao<T>
        extends ConditionBuilder<DeleteDao<T>> {

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
     * 如果删除的记录数不是 > 1 条，将抛出异常，回滚
     *
     * @return
     */
    @Transactional
    boolean singleDelete();

    /**
     * 批量更新，直到所有的记录都更新完成
     *
     * @param batchCommitSize
     * @return
     */
    @Transactional(propagation = Propagation.NEVER)
    int batchDelete(int batchCommitSize);

//    /**
//     * 逻辑删除
//     * 如果不支持逻辑删除，则直接返回 0
//     *
//     * @return
//     */
//    @Transactional
//    int logicDelete();

}
