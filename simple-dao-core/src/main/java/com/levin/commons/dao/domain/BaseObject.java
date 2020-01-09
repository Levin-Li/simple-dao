package com.levin.commons.dao.domain;


/**
 * 基本对象
 *
 * @param <ID>
 */
public interface BaseObject<ID>
        extends
        Identifiable<ID>,
        EnableObject,
        EditableObject,
        OrderableObject {

}
