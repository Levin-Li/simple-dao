package com.levin.commons.dao.domain;


import java.io.Serializable;

/**
 * 实体对象，通常是指ORM中的实体对象
 *
 * @param <ID>
 */
public interface EntityObject<ID extends Serializable>
        extends
        Identifiable<ID>,
        Serializable,
        Cloneable {

}
