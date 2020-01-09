package com.levin.commons.dao;


/**
 * 分页对象
 */

public interface Paging {

    /**
     * 如果为负数表示不限制
     *
     * @param pageCount
     * @return
     */
    Paging setPageCount(int pageCount);

    /**
     * 总页数
     *
     * @return
     */
    int getPageCount();

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

    /**
     * 获取下一页
     *
     * @return
     */
   // Paging next();

    /**
     * 获取上一页
     *
     * @return
     */
  //  Paging prev();

    /**
     * 跳到指定的页
     *
     * @param pageIndex
     * @return
     */
  //  Paging go(int pageIndex);

}
