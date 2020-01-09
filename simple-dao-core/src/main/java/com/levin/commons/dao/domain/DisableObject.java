package com.levin.commons.dao.domain;


/**
 * 是否是可用的对象
 * <p/>
 * 1 表示启用
 * 0 表示禁用
 */
public interface DisableObject {

    /**
     * 是否禁用
     *
     * @return isDisable
     */
    boolean isDisable();

}
