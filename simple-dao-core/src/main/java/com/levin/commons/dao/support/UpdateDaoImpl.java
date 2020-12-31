package com.levin.commons.dao.support;


import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.UpdateDao;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * 更新Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class UpdateDaoImpl<T>
        extends ConditionBuilderImpl<T, UpdateDao<T>>
        implements UpdateDao<T> {

    transient MiniDao dao;

    final SimpleList<String> updateColumns = new SimpleList<>(true, new ArrayList(5), " , ");

    final List updateParamValues = new ArrayList(7);


    static final String UPDATE_PACKAGE_NAME = Update.class.getPackage().getName();

    boolean throwExWhenNoColumnForUpdate = true;

    public UpdateDaoImpl() {
        this(true, null);
    }

    public UpdateDaoImpl(boolean isNative, MiniDao dao) {
        super(isNative);
        this.dao = dao;
    }

    public UpdateDaoImpl(MiniDao dao, Class<T> entityClass, String alias) {
        super(entityClass, alias);
        this.dao = dao;
    }

    public UpdateDaoImpl(MiniDao dao, String tableName, String alias) {
        super(tableName, alias);
        this.dao = dao;
    }


    @Override
    protected MiniDao getDao() {
        return dao;
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

        String whereStatement = genWhereStatement();

        ql.append(" Update ")
                .append(genEntityStatement())
                .append(" Set ")
                .append(updateColumns)
                .append(whereStatement);

        //安全模式
        if (this.isSafeMode() && !hasText(whereStatement) && !isSafeLimit()) {
            throw new StatementBuildException("Safe mode not allow no where statement or limit [" + rowCount + "] too large, safeModeMaxLimit[1 - " + getDao().safeModeMaxLimit() + "], SQL[" + ql + "]");
        }

        return ExprUtils.replace(ql.toString(), getDaoContextValues());
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
        return QueryAnnotationUtil.flattenParams(null, getDaoContextValues(), updateParamValues, whereParamValues);
    }

    @Transactional
    @Override
    public int update() {

        if (!hasColumnsForUpdate() && !throwExWhenNoColumnForUpdate) {

            logger.warn("忽略没有要更新列的更新语句[" + genFinalStatement() + "]");

            return -1;
        }

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
