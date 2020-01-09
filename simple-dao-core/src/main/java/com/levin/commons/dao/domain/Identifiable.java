package com.levin.commons.dao.domain;


/**
 * 可标识的对象
 *
 * @param <ID>
 */
public interface Identifiable<ID> {

    /**
     * 获取对象标识
     *
     * @return id
     */
    ID getId();

}
