package com.levin.commons.dao.domain;


/**
 * 可标识的对象
 *
 * @param <ID>
 */
public interface Identifiable<ID> {

    /**
     * ID 常量，方便 Lombok 用于 @EqualsAndHashCode 注解
     */
    String ID = "id";

    /**
     * 获取对象标识
     *
     * @return id
     */
    ID getId();

}
