package com.levin.commons.dao;


/**
 * 逻辑操作结果
 *
 * @param <T>
 */
public interface LogicOP<T> {

    /**
     * 开始与条件
     *
     * @return
     */
    T and();

    /**
     * 开始或条件
     *
     * @return
     */
    T or();


    /**
     * 结束
     *
     * @return
     */
    T end();

}
