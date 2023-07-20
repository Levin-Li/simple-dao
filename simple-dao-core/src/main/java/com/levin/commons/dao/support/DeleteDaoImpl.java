package com.levin.commons.dao.support;


import com.levin.commons.dao.DaoSecurityException;
import com.levin.commons.dao.DeleteDao;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.util.ExceptionUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NonUniqueResultException;
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
                    .append(genWhereStatement(EntityOption.Action.LogicalDelete));
        } else {
            ql.append("Delete ")
                    .append(genFromStatement())
                    .append(genWhereStatement(EntityOption.Action.Delete));
        }

        ql.append(" ")
                .append(lastStatements.isEmpty() ? getLimitStatement() : lastStatements);

        return replaceVar(ql.toString());
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
                , whereParamValues, getLastStatementParamValues());

        if (isLogicDelete) {
            flattenParams.add(0, convertLogicDeleteValue(getEntityOption()));
        }

        return flattenParams;
    }



    @Override
    @Transactional
    public int delete() {

        EntityOption entityOption = getEntityOption();

        boolean disableDel = isDisable(EntityOption.Action.Delete);
        boolean disableLogicDel = isDisable(EntityOption.Action.LogicalDelete);

        if (disableDel && disableLogicDel) {
            throw new DaoSecurityException("" + entityClass + " disable delete action");
        }

        boolean hasLogicDeleteField = hasLogicDeleteField(entityOption);

        if (!disableDel && !hasLogicDeleteField) {
            //如果能物理删除，但又没有逻辑删除的字段，那么只能物理删除
            return batchDelete(genFinalStatement(false), genFinalParamList(false));
        }

        Exception ex = null;

        if (!disableDel) {
            ////如果能物理删除，先尝试物理删除
            try {
                // if (true) throw new StatementBuildException("mock delete error");
                return batchDelete(genFinalStatement(false), genFinalParamList(false));
            } catch (Exception e) {
                ex = e;
            }
        }

        if (disableLogicDel) {
            //如果不允许逻辑删除
            reThrow(ex);
        }

        //接下来尝试逻辑删除

        int n = batchDelete(genFinalStatement(true), genFinalParamList(true));

        if (n > 0) {
            if (ex != null) {
                logger.warn("delete " + entityClass + " error ,"
                        + ExceptionUtils.getAllCauseInfo(ex, " -> "));
            }
            return n;
        }

        return n;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean singleDelete() {

        setRowCount(2);

        int n = delete();

        if (n > 1) {
            throw new NonUniqueResultException(n + "条记录被删除，预期1条");
        }

        return n == 1;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void uniqueDelete() {

        setRowCount(2);

        int n = delete();

        if (n != 1) {
            throw new NonUniqueResultException(n + "条记录被删除，预期有且仅有1条");
        }
    }

    /**
     * @param statement
     * @param paramList
     * @return
     */
    int batchDelete(String statement, List paramList) {
        return dao.update(isNative(), rowStart, rowCount, statement, paramList);
    }

    private void reThrow(Exception ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else if (ex != null) {
            throw new RuntimeException(ex);
        }
    }
}
