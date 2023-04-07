package com.levin.commons.dao;


/**
 * 逻辑操作结果
 * <p>
 * 为了编译兼容，只是增加了方法
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
     * 开始与条件
     * <p>
     * 为了保持链式调用而增加的方法
     *
     * @return
     */
    T and(Boolean valid);

    /**
     * 开始或条件
     *
     * @return
     */
    T or();

    /**
     * 开始或条件
     * <p>
     * 为了保持链式调用而增加的方法
     *
     * @return
     */
    T or(Boolean valid);

    /**
     * 结束
     *
     * @return
     */
    T end();

}
