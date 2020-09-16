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

}
