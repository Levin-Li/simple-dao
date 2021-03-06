package com.levin.commons.dao.support;


import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.UpdateDao;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.EditableObject;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class UpdateDaoImpl<T>
        extends ConditionBuilderImpl<T, UpdateDao<T>>
        implements UpdateDao<T> {


    final SimpleList<String> updateColumns = new SimpleList<>(true, new ArrayList(5), " , ");

    final List updateParamValues = new ArrayList(7);


    static final String UPDATE_PACKAGE_NAME = Update.class.getPackage().getName();

    boolean throwExWhenNoColumnForUpdate = true;

    {
        //默认为安全模式
        safeMode = true;
    }

    public UpdateDaoImpl() {
        this(null, true);
    }

    public UpdateDaoImpl(MiniDao dao, boolean isNative) {
        super(dao, isNative);
    }

    public UpdateDaoImpl(MiniDao dao, boolean isNative, Class<T> entityClass, String alias) {
        super(dao, isNative, entityClass, alias);
    }

    public UpdateDaoImpl(MiniDao dao, boolean isNative, String tableName, String alias) {
        super(dao, isNative, tableName, alias);
    }

    @Override
    public UpdateDao<T> setColumns(String columns, Object... paramValues) {

        return setColumns(true, columns, paramValues);
    }

    @Override
    public UpdateDao<T> setColumns(Boolean isAppend, String columns, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && StringUtils.hasText(columns)) {
            append(columns, paramValues);
        }

        return this;
    }


    @Override
    public UpdateDao<T> set(String entityAttrName, Object paramValue) {
        return set(true, entityAttrName, paramValue);
    }


    @Override
    public UpdateDao<T> set(Boolean isAppend, String entityAttrName, Object paramValue) {
        if (Boolean.TRUE.equals(isAppend)
                && StringUtils.hasText(entityAttrName)) {
            append(aroundColumnPrefix(entityAttrName) + " = " + getParamPlaceholder(), paramValue);
        }
        return this;
    }

    private void append(String expr, Object... values) {
        if (updateColumns.add(expr)) {
            updateParamValues.add(values);
        }
    }


    @Override
    public UpdateDao<T> disableThrowExWhenNoColumnForUpdate() {

        throwExWhenNoColumnForUpdate = false;

        return this;
    }

    @Override
    public String genFinalStatement() {

        StringBuilder ql = new StringBuilder();

        //没有需要更新的字段
        if (updateColumns.length() == 0 && throwExWhenNoColumnForUpdate) {
            throw new StatementBuildException("no columns to update");
        }

        String whereStatement = genWhereStatement(EntityOption.Action.Update);

        ql.append(" Update ")
                .append(genEntityStatement())
                .append(" Set ")
                .append(updateColumns)
                .append(whereStatement)
                .append(" ").append(lastStatements)
                .append(getLimitStatement());

        return replaceVar(ql.toString());
    }

    /**
     * 是否有要更新的列
     * <p>
     * 2018.3.30 增加
     *
     * @return
     */
    @Override
    public boolean hasColumnsForUpdate() {
        return (updateColumns.length() > 0);
    }


    @Override
    public List genFinalParamList() {
        return QueryAnnotationUtil.flattenParams(null, getDaoContextValues(), updateParamValues, whereParamValues, lastStatementParamValues);
    }

    @Transactional
    @Override
    public int update() {

        if (!hasColumnsForUpdate() && !throwExWhenNoColumnForUpdate) {

            logger.warn("忽略没有要更新列的更新语句[" + genFinalStatement() + "]");

            return -1;
        }

        checkAction(EntityOption.Action.Update, null);

        return dao.update(isNative(), rowStart, rowCount, genFinalStatement(), genFinalParamList());
    }


    @Override
    public void processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name,
                                Class<?> varType, Object value, Annotation opAnnotation) {

//        if (isIgnore(varAnnotations)) {
//            return;
//        }

        if (isPackageStartsWith(UPDATE_PACKAGE_NAME, opAnnotation)) {

            genExprAndProcess(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation, (expr, holder) -> {
                setColumns(expr, holder.value);
            });

        }

        //允许 Update 注解和其它注解同时存在

        super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

    }

}
