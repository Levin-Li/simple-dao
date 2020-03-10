package com.levin.commons.dao;

/**
 * 在安全模式下，不允许无条件的更新、删除、查询
 * <p>
 * 强烈推荐
 *
 * @date 2019.04.25
 * @since 2.0.0
 */
public interface SafeController<T> {

    /**
     * 禁止安全模式
     */
    T disableSafeMode();

    /**
     * 安全模式
     * <p>
     * 在安全模式下，不允许无条件的查询、更新和删除
     *
     * @return
     */
    boolean isSafeMode();

}
