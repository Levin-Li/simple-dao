package com.levin.commons.dao;


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


//    /**
//     * 逻辑删除
//     * 如果不支持逻辑删除，则直接返回 0
//     *
//     * @return
//     */
//    @Transactional
//    int logicDelete();

}
