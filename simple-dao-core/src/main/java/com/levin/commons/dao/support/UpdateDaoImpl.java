package com.levin.commons.dao.support;


import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.UpdateDao;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

//    @Override
//    protected String getParamPlaceholder() {
//        return dao.getParamPlaceholder(isNative());
//    }

    @Override
    protected MiniDao getDao() {
        return dao;
    }

    @Override
    public UpdateDao<T> setColumns(String columns, Object... paramValues) {

        this.updateColumns.clear();
        this.updateParamValues.clear();

        return appendColumns(columns, paramValues);
    }

    @Override
    public UpdateDao<T> appendColumns(String columns, Object... paramValues) {

        if (StringUtils.hasText(columns)) {
            append(columns, paramValues);
        }

        return this;
    }

    @Override
    public UpdateDao<T> appendColumns(Boolean isAppend, String columns, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)) {
            return appendColumns(columns, paramValues);
        }

        return this;
    }

    @Override
    public UpdateDao<T> appendColumn(String entityAttrName, Object paramValue) {

        if (StringUtils.hasText(entityAttrName)) {
            append(aroundColumnPrefix(entityAttrName) + " = " + getParamPlaceholder(), paramValue);
        }

        return this;
    }

    @Override
    public UpdateDao<T> appendColumn(Boolean isAppend, String entityAttrName, Object paramValue) {

        if (Boolean.TRUE.equals(isAppend)) {
            return appendColumn(entityAttrName, paramValue);
        }

        return this;
    }


    @Override
    public UpdateDao<T> set(String entityAttrName, Object paramValue) {
        return appendColumn(entityAttrName, paramValue);
    }


    @Override
    public UpdateDao<T> set(Boolean isAppend, String entityAttrName, Object paramValue) {
        return appendColumn(isAppend, entityAttrName, paramValue);
    }

    private void append(String expr, Object... values) {
        if (updateColumns.add(expr)) {
            updateParamValues.add(values);
        }
    }

    @Override
    public UpdateDao<T> appendColumnByDTO(Object dto, String... ignoreAttrs) {
        return appendColumnByDTO(true, dto, ignoreAttrs);
    }

    @Override
    public UpdateDao<T> appendColumnByDTO(final boolean ignoreNullValueColumn, Object dto, final String... ignoreAttrs) {

        walkObject(new AttrCallback() {
            @Override
            public boolean onAction(Object bean, Object fieldOrMethod, String name, Annotation[] varAnnotations, Class<?> attrType, Object value) {

                if (!StringUtils.hasText(name)) {
                    return true;
                }

                if (ignoreAttrs != null) {
                    for (String ignoreAttr : ignoreAttrs) {
                        if (name.equals(ignoreAttr)) {
                            return true;
                        }
                    }
                }

                //如果忽略空值，且值为空
                if (value == null) {
                    if (ignoreNullValueColumn) {
                        return true;
                    }
                } else if (isComplexType(null, value)) {
                    //如果不是原子类型
                    if (logger.isDebugEnabled()) {
                        logger.debug(bean.getClass() + "[" + bean + "]" + " attr [" + name + "] value is not a primitive,it will be ignore.");
                    }
                    return true;
                }

                appendColumn(name, value);

                return true;
            }
        }, dto);

        return this;
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
        if (this.isSafeMode() && !StringUtils.hasText(whereStatement)) {
            throw new StatementBuildException("safe mode not allow no where statement SQL[" + ql + "]");
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

        if (isPackageStartsWith(UPDATE_PACKAGE_NAME, opAnnotation)) {

            PrimitiveValue primitiveValue = QueryAnnotationUtil.findFirstMatched(varAnnotations, PrimitiveValue.class);

            genExprAndProcess(bean, varType, name, value, primitiveValue, opAnnotation, (expr, holder) -> {
                appendColumns(expr, holder.value);
            });

        }

        //允许 Update 注解和其它注解同时存在

        super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);


    }


}
