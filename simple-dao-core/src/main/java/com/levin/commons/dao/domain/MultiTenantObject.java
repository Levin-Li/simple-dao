package com.levin.commons.dao.domain;

import java.io.Serializable;

/**
 * 多租户对象
 *
 * @param <ID>
 * @Author levin li
 * @Since 2.2.23
 */
public interface MultiTenantObject<ID extends Serializable> {

    /**
     * 获取租户 ID
     *
     * @return
     */
    ID getTenantId();

}
