package com.levin.commons.dao;


/**
 * 分页接口
 */

public interface Paging {

    /**
     * 是否要返回记录总数
     *
     * @return
     */
    default boolean isRequireTotals() {
        return false;
    }

    /**
     * 是否要返回结果集
     *
     * @return
     */
    default boolean isRequireResultList() {
        return true;
    }

    /**
     * 如果为负数表示不限制
     * <p>
     * 获取页面记录数大小
     *
     * @return
     */
    int getPageSize();

    /**
     * 当前页序号
     * 从1开始
     * <p>
     * 如果为负数表示不限制
     *
     * @return
     */
    int getPageIndex();

}
