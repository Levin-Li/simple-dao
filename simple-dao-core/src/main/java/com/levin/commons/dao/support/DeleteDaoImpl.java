package com.levin.commons.dao.support;


import com.levin.commons.dao.DeleteDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 删除Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class DeleteDaoImpl<T>
        extends ConditionBuilderImpl<T, DeleteDao<T>>
        implements DeleteDao<T> {

    transient MiniDao dao;

    public DeleteDaoImpl() {
        this(true, null);
    }

    protected DeleteDaoImpl(boolean isNative, MiniDao dao) {
        super(isNative);
        this.dao = dao;
    }

    public DeleteDaoImpl(MiniDao dao, Class<T> entityClass, String alias) {
        super(entityClass, alias);
        this.dao = dao;
    }

    public DeleteDaoImpl(MiniDao dao, String tableName, String alias) {
        super(tableName, alias);
        this.dao = dao;
    }

//    @Override
//    protected String getParamPlaceholder() {
//        return dao.getParamPlaceholder(isNative());
//    }

    @Override
    protected MiniDao getDao() {
        return dao;
    }

    @Override
    public String genFinalStatement() {


        String whereStatement = genWhereStatement();


        String ql = "Delete " + genFromStatement() + whereStatement;


        if (this.isSafeMode() && whereStatement.trim().length() == 0) {
            throw new StatementBuildException("safe mode not allow no where statement SQL[" + ql + "]");
        }

        return ql;
    }


    @Override
    public List genFinalParamList() {

        //增加环境参数

        return QueryAnnotationUtil.flattenParams(null
                , getDaoContextValues()
                , whereParamValues);
    }

    @Override
    @Transactional
    public int delete() {
        return dao.update(isNative(), rowStart, rowCount, genFinalStatement(), genFinalParamList());
    }

}
