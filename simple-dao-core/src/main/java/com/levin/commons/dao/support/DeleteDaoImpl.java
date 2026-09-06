package com.levin.commons.dao.support;


import com.levin.commons.dao.exception.DaoSecurityException;
import com.levin.commons.dao.DeleteDao;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.utils.ExceptionUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 删除Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class DeleteDaoImpl<T>
        extends ConditionBuilderImpl<DeleteDao<T>, T>
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

        // Hibernate 的多表继承删除在设置 MaxResults 时会触发 CTE limit 参数未绑定缺陷。
        // 先查询最多两条记录保留单条删除保护，再执行不设置 MaxResults 的删除。
        int n = testDeleteRows(2);

        if (n > 1) {
            throw new IncorrectResultSizeDataAccessException(n + "条记录会被预期删除，预期小于等于1条", 1, n);
        }

        int maxResult = getRowCount();
        setRowCount(-1);
        disableSafeMode();

        try {
            n = delete();
        } finally {
            setRowCount(maxResult);
            this.safeMode = true;
        }

        if (n > 1) {
            throw new IncorrectResultSizeDataAccessException(n + "条记录被删除，预期小于等于1条", 1, n);
        }

        return n == 1;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void uniqueDelete() {

        // Hibernate 的多表继承删除在设置 MaxResults 时会触发 CTE limit 参数未绑定缺陷。
        // 先查询最多两条记录保留唯一删除保护，再执行不设置 MaxResults 的删除。
        int n = testDeleteRows(2);

        if (n != 1) {
            throw new IncorrectResultSizeDataAccessException(n + "条记录会被预期删除，预期有且仅有1条", 1, n);
        }

        int maxResult = getRowCount();
        setRowCount(-1);
        disableSafeMode();

        try {
            n = delete();
        } finally {
            setRowCount(maxResult);
            this.safeMode = true;
        }

        if (n != 1) {
            throw new IncorrectResultSizeDataAccessException(n + "条记录被删除，预期有且仅有1条", 1, n);
        }
    }

    /**
     * @param statement
     * @param paramList
     * @return
     */
    int batchDelete(String statement, List paramList) {
        try {
            dao.setCurrentThreadMaxLimit(isSafeMode() ? getSafeModeMaxLimit() : -1);
            return dao.update(isNative(), rowStart, rowCount, statement, paramList);
        } finally {
            dao.setCurrentThreadMaxLimit(null);
        }
    }

    protected int testDeleteRows(int maxResult) {

        EntityOption.Action action = isDisable(EntityOption.Action.Delete)
                ? EntityOption.Action.LogicalDelete
                : EntityOption.Action.Delete;

        String jpql = replaceVar(" Select 1 From "
                + genEntityStatement()
                + genWhereStatement(action)
                + " " + (lastStatements.isEmpty() ? getLimitStatement() : lastStatements));

        List<Object> resultList = dao.find(isNative(), null, 0, maxResult, jpql,
                QueryAnnotationUtil.flattenParams(null, getDaoContextValues(), whereParamValues, getLastStatementParamValues()));

        return resultList.size();
    }

    private void reThrow(Exception ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else if (ex != null) {
            throw new RuntimeException(ex);
        }
    }
}
