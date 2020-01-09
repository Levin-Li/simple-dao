package com.levin.commons.dao.domain;


/**
 * 可排序的对象
 */
public interface OrderableObject<T> {

    /**
     * 排序代码
     *
     * @return
     */
    T getOrderCode();

}
