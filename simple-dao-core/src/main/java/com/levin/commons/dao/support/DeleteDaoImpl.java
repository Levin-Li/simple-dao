package com.levin.commons.dao.support;


import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.util.ExceptionUtils;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * 删除Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class DeleteDaoImpl<T>
        extends ConditionBuilderImpl<T, DeleteDao<T>>
        implements DeleteDao<T> {


    {
        //默认为安全模式
        safeMode = true;
    }

    public DeleteDaoImpl() {
        this(null, true);
    }

    public DeleteDaoImpl(MiniDao dao, boolean isNative) {
        super(dao, isNative);
    }

    public DeleteDaoImpl(MiniDao dao, boolean isNative, Class<T> entityClass, String alias) {
        super(dao, isNative, entityClass, alias);
    }

    public DeleteDaoImpl(MiniDao dao, boolean isNative, String tableName, String alias) {
        super(dao, isNative, tableName, alias);
    }

//    @Override
//    protected String getParamPlaceholder() {
//        return dao.getParamPlaceholder(isNative());
//    }

    @Override
    public String genFinalStatement() {

        boolean disableDel = isDisable(EntityOption.Action.Delete);
        boolean disableLogicDel = isDisable(EntityOption.Action.LogicalDelete);

        if (disableDel && disableLogicDel) {
            throw new DaoSecurityException(" " + entityClass + " disable delete action");
        }

        return genFinalStatement(!disableDel);
    }

    private final String genFinalStatement(boolean isLogicDelete) {

        StringBuilder ql = new StringBuilder();

        if (isLogicDelete) {

            ql.append("Update ")
                    .append(genEntityStatement())
                    .append(" Set ")
                    .append(genLogicDeleteExpr(getEntityOption(), Op.Eq))
                    .append(genWhereStatement(EntityOption.Action.LogicalDelete))
                    .append(" ").append(lastStatements)
                    .append(getLimitStatement());
        } else {
            ql.append("Delete ")
                    .append(genFromStatement())
                    .append(genWhereStatement(EntityOption.Action.Delete))
                    .append(" ").append(lastStatements)
                    .append(getLimitStatement());

        }

        return ExprUtils.replace(ql.toString(), getDaoContextValues());

    }

    @Override
    public List genFinalParamList() {
        //增加环境参数
        return genFinalParamList(!isDisable(EntityOption.Action.Delete));
    }

    public List genFinalParamList(boolean isLogicDelete) {
        //增加环境参数

        List flattenParams = QueryAnnotationUtil.flattenParams(null
                , getDaoContextValues()
                , whereParamValues, lastStatementParamValues);

        if (isLogicDelete) {
            flattenParams.add(0, convertLogicDeleteValue(getEntityOption()));
        }

        return flattenParams;
    }

    @Override
    @Transactional
    public int delete() {

        boolean disableDel = isDisable(EntityOption.Action.Delete);
        boolean disableLogicDel = isDisable(EntityOption.Action.LogicalDelete);

        if (disableDel && disableLogicDel) {
            throw new DaoSecurityException("" + entityClass + " disable delete action");
        }

        boolean hasLogicDeleteField = hasLogicDeleteField();

        if (!disableDel && !hasLogicDeleteField) {
            //如果能物理删除，但又没有逻辑删除的字段，那么只能物理删除
            return dao.update(isNative(), rowStart, rowCount, genFinalStatement(false), genFinalParamList(false));
        }

        Exception ex = null;

        if (!disableDel) {
            ////如果能物理删除，先尝试物理删除
            try {
                // if (true) throw new StatementBuildException("mock delete error");
                return dao.update(isNative(), rowStart, rowCount, genFinalStatement(false), genFinalParamList(false));
            } catch (Exception e) {
                ex = e;
            }
        }

        if (disableLogicDel) {
            //如果不允许逻辑删除
            reThrow(ex);
        }

        //接下来尝试逻辑删除

        int n = dao.update(isNative(), rowStart, rowCount, genFinalStatement(true), genFinalParamList(true));

        if (n > 0) {
            if (ex != null) {
                logger.warn("delete " + entityClass + " error ,"
                        + ExceptionUtils.getAllCauseInfo(ex, " -> "));
            }
            return n;
        }

        return n;

    }

    private void reThrow(Exception ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else if (ex != null) {
            throw new RuntimeException(ex);
        }
    }
}
