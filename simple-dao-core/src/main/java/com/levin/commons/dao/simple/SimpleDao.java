package com.levin.commons.dao.simple;


import com.levin.commons.dao.simple.annotation.*;
import com.levin.commons.dao.support.RepositoryFactoryBean;

import java.util.List;

/**
 * 通用DAO服务
 */
@EntityRepository
public abstract class SimpleDao {

    /**
     * 查询
     *
     * @param queryObjs 查询对象 支持TargetEntity注解明确查询目标
     * @return 可能是返回单个结果或是List
     */
    @QueryRequest
    public <T> List<T> find(Object... queryObjs) {
        return RepositoryFactoryBean.getProxyInvokeResult();
    }


    /**
     * 查询
     *
     * @param queryObjs 查询对象 支持TargetEntity注解明确查询目标
     * @return 可能是返回单个结果或是List
     */
    @QueryRequest
    public <T> T findOne(Object... queryObjs) {
        return RepositoryFactoryBean.getProxyInvokeResult();
    }

    /**
     * 查询
     *
     * @param queryObjs 查询对象 支持TargetEntity注解明确查询目标
     * @return
     */
    @UpdateRequest
    public int update(Object... queryObjs) {
        return RepositoryFactoryBean.getProxyInvokeResult();
    }

    /**
     * 查询
     *
     * @param queryObjs 查询对象 支持TargetEntity注解明确查询目标
     * @return
     */
    @DeleteRequest
    public int delete(Object... queryObjs) {
        return RepositoryFactoryBean.getProxyInvokeResult();
    }

}
