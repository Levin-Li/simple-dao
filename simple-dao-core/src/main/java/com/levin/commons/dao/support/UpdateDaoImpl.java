package com.levin.commons.dao.support;


import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.UpdateDao;
import com.levin.commons.dao.annotation.update.UpdateColumn;
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

    public UpdateDaoImpl() {
        this(true, null);
    }

    protected UpdateDaoImpl(boolean isNative, MiniDao dao) {
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
    public int getParamStartIndex() {
        return dao.getParamStartIndex();
    }

    @Override
    public UpdateDao<T> setColumns(String columns, Object... paramValues) {

        this.updateColumns.clear();
        this.updateParamValues.clear();

        return appendColumns(columns, paramValues);
    }

    @Override
    public UpdateDao<T> appendColumns(String columns, Object... paramValues) {

        if (StringUtils.hasText(columns))
            append(columns, paramValues);

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

        if (StringUtils.hasText(entityAttrName))
            append(aroundColumnPrefix(entityAttrName) + " = " + getParamPlaceholder(null), paramValue);

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
        if (updateColumns.add(expr))
            updateParamValues.add(values);
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

                if (!StringUtils.hasText(name))
                    return true;

                if (ignoreAttrs != null) {
                    for (String ignoreAttr : ignoreAttrs) {
                        if (name.equals(ignoreAttr))
                            return true;
                    }
                }

                //如果忽略空值，且值为空
                if (value == null) {
                    if (ignoreNullValueColumn)
                        return true;
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
    public String genFinalStatement() {

        StringBuilder ql = new StringBuilder();

        if (updateColumns.length() == 0)
            throw new StatementBuildException("no columns to update");


        String whereStatement = genWhereStatement();


        ql.append(" Update ")
                .append(genEntityStatement())
                .append(" Set ")
                .append(updateColumns)
                .append(whereStatement);

        //安全模式
        if (this.isSafeMode() && whereStatement.trim().length() == 0) {
            throw new StatementBuildException("safe mode not allow no where statement SQL[" + ql + "]");
        }


        return ql.toString();

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
        return dao.update(isNative(), rowStart, rowCount, replacePlaceholder(genFinalStatement()), genFinalParamList());
    }


    @Override
    public boolean processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        processUpdateAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        return super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);
    }

    /**
     * @param bean
     * @param fieldOrMethod
     * @param varAnnotations
     * @param name
     * @param varType
     * @param value
     * @param opAnnotation
     */
    protected void processUpdateAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        if (!(opAnnotation instanceof UpdateColumn))
            return;

        UpdateColumn anno = (UpdateColumn) opAnnotation;

        //如果忽略空值
        if (anno.useVarValue()
                && anno.ignoreNullValue()
                && value == null)
            return;

        String expr = "";

        boolean isSubQuery = false;

        boolean complexType = !hasPrimitiveAnno(varAnnotations) && isComplexType(varType, value);

        ValueHolder holder = new ValueHolder(bean, value);

        //子查询的方式
        if (StringUtils.hasText(anno.subQuery())) {

            isSubQuery = true;
            expr = anno.subQuery();

            expr = doReplace(expr, anno.useVarValue(), holder);

            value = holder.value;

        } else if (complexType) {
            isSubQuery = true;
            //复杂对象子查询的方式
            expr = buildSubQuery(holder);
            value = holder.value;
        } else {
            expr = (anno.useVarValue() ? getParamPlaceholder(null) : "");
            value = anno.useVarValue() ? value : new Object[0];
        }

        //如果是子查询加上 as
        if (isSubQuery) {
            expr = autoAroundParentheses(anno.prefix(), expr, anno.suffix());
        } else {
            expr = anno.prefix() + expr + anno.suffix();
        }

        //必须是原子类型的字段或是空值
        //语句的组成: value = op + prefix + ? + suffix
        expr = aroundColumnPrefix(name) + " = " + anno.op() + expr;

        appendColumns(expr, value);

    }

}
