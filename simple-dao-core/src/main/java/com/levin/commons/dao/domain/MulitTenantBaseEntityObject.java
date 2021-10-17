package com.levin.commons.dao.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 多租户
 * @param <ID>
 */
public interface MulitTenantBaseEntityObject<ID extends Serializable>
        extends BaseEntityObject<ID> {

}
