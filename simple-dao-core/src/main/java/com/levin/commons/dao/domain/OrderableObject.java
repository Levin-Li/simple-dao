package com.levin.commons.dao.domain;


/**
 * 可排序的对象
 */
public interface OrderableObject<C extends Comparable> {

    /**
     * 排序代码
     *
     * @return
     */
    C getOrderCode();

}
