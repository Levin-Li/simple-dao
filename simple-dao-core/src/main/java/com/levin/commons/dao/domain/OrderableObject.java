package com.levin.commons.dao.domain;


/**
 * 可排序的对象
 */
public interface OrderableObject<T extends Comparable> extends Comparable<OrderableObject<T>> {

    /**
     * 排序代码
     *
     * @return
     */
    T getOrderCode();

    @Override
    default int compareTo(OrderableObject<T> o) {

        if (o == null) {
            return 1;
        }

        if (getOrderCode() == null) {
            return o.getOrderCode() == null ? 0 : -1;
        }

        return getOrderCode().compareTo(o.getOrderCode());
    }
}
