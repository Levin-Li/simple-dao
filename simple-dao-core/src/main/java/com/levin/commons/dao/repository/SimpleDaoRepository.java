package com.levin.commons.dao.repository;


import com.levin.commons.dao.repository.annotation.DeleteRequest;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.repository.annotation.QueryRequest;
import com.levin.commons.dao.repository.annotation.UpdateRequest;

import java.util.List;

/**
 * 通用DAO服务
 */
@EntityRepository
public abstract class SimpleDaoRepository {

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
