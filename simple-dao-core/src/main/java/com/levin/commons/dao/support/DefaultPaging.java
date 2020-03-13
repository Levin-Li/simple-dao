package com.levin.commons.dao.support;

import com.levin.commons.dao.Paging;
import com.levin.commons.dao.annotation.Ignore;

import java.io.Serializable;

/**
 *
 */


public class DefaultPaging
        implements Paging, Serializable {

    @Ignore
    int pageCount;

    @Ignore
    int pageIndex;

    @Ignore
    int pageSize = 20;


    public DefaultPaging() {
    }

    public DefaultPaging(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    @Override
    public int getPageCount() {
        return pageCount;
    }

    @Override
    public DefaultPaging setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    @Override
    public int getPageIndex() {
        return pageIndex;
    }

    public DefaultPaging setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public DefaultPaging setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }


    /**
     * 获取下一页
     *
     * @return
     */
//    @Override
    public Paging next() {
        pageIndex++;
        return this;
    }

    /**
     * 获取上一页
     *
     * @return
     */
//    @Override
    public Paging prev() {
        pageIndex--;
        return this;
    }

    /**
     * 跳到指定的页
     *
     * @param pageIndex
     * @return
     */
//    @Override
    public Paging go(int pageIndex) {

        this.pageIndex = pageIndex;

        return this;
    }

}
