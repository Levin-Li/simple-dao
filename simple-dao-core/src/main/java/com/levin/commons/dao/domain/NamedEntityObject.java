package com.levin.commons.dao.domain;


import java.io.Serializable;

/**
 * 命名的实体对象
 *
 * @param <ID>
 */
public interface NamedEntityObject<ID extends Serializable>
        extends
        BaseEntityObject<ID>,
        NamedObject {
}
