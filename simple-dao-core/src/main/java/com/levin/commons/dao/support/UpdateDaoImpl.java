package com.levin.commons.dao.support;


import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NonUniqueResultException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimWhitespace;

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
    public UpdateDao<T> setColumns(Boolean isAppend, String columns, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && hasText(columns)) {
            append(columns, paramValues);
        }

        return this;
    }

    @Override
    public UpdateDao<T> setNull(Boolean isAppend, String... entityAttrNames) {

        if (Boolean.TRUE.equals(isAppend) && entityAttrNames != null) {
            for (String entityAttrName : entityAttrNames) {
                if (hasText(entityAttrName)) {
                    append(aroundColumnPrefix(entityAttrName) + " = NULL");
                }
            }
        }

        return this;
    }

    @Override
    public UpdateDao<T> set(Boolean isAppend, String entityAttrName, Object paramValue) {
        if (Boolean.TRUE.equals(isAppend)
                && hasText(entityAttrName)) {
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
                .append(" ").append(lastStatements.isEmpty() ? getLimitStatement() : lastStatements);

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
        return QueryAnnotationUtil.flattenParams(null, getDaoContextValues(), updateParamValues, whereParamValues, getLastStatementParamValues());
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public int update() {

        checkAction(EntityOption.Action.Update, null);

        String statement = genFinalStatement();

        if (!hasColumnsForUpdate() && !throwExWhenNoColumnForUpdate) {
            logger.warn("忽略没有要更新列的更新语句[" + statement + "]");
            return -1;
        }

        return dao.update(isNative(), rowStart, rowCount, statement, genFinalParamList());
    }

    /**
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean singleUpdate() {

        //允许1条，
        setRowCount(2);

        int n = update();

        if (n > 1) {
            throw new NonUniqueResultException(n + "条记录被更新，预期1条");
        }

        return n == 1;
    }

    /**
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void uniqueUpdate() {

        //允许2条，
        setRowCount(2);

        int n = update();

        if (n != 1) {
            throw new NonUniqueResultException(n + "条记录被更新，预期有且仅有1条");
        }

    }

    @Override
    public void processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name,
                                Class<?> varType, Object value, Annotation opAnnotation) {

//        if (isIgnore(varAnnotations)) {
//            return;
//        }

        if (isPackageStartsWith(UPDATE_PACKAGE_NAME, opAnnotation)) {


            genExprAndProcess(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation, (expr, holder) -> {

                Update updateOp = (opAnnotation instanceof Update) ? (Update) opAnnotation : null;

                if (updateOp != null) {

                    //设置更新的where条件
                    String whereCondition = updateOp.whereCondition();

                    if (hasText(whereCondition)) {
                        whereCondition = tryEvalExprIfHasSeplExpr(bean, value, name, whereCondition, getDaoContextValues(), getContext());
                        where(whereCondition);
                    }

                    //如果是增量更新
                    if (updateOp.incrementMode()
                            //去除成对的小括号
                            && hasText(expr = ExprUtils.trimParenthesesPair(expr))
                            && expr.indexOf('=') != -1) {

                        expr = genAssignExpr(getDao(), updateOp, name, varType, expr, holder);
                    }
                }

                //设置更新的列
                setColumns(expr, holder.value);

            });
        }

        //允许 Update 注解和其它注解同时存在

        super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

    }


    /**
     * 生成增量更新语句
     *
     * @param varType
     * @param expr
     * @return
     */
    private String genAssignExpr(MiniDao dao, Update updateOp, String name, Class<?> varType, String expr, ValueHolder<Object> holder) {

        Class<?> fieldType = fieldType = QueryAnnotationUtil.getFieldType(entityClass, name);

        if (fieldType == null) {
            fieldType = (holder.value != null && BeanUtils.isSimpleValueType(holder.value.getClass()) ? holder.value.getClass() : null);
        }

        //如果类型正确
        if (fieldType == null && varType != null && BeanUtils.isSimpleValueType(varType)) {
            fieldType = varType;
        }

        //数据库字段的类型
        final Class<?> dbColumnType = fieldType;

        final Supplier<StatementBuildException> exSupplier = () -> new StatementBuildException("increment update can't support type[" + dbColumnType + "]，only Number or String");

        Assert.notNull(dbColumnType, exSupplier);

        //检查是否非法的语句
        //Assert.isTrue(expr.charAt(0) != '(',() -> new StatementBuildException("非法的语句"));

        int indexOf = expr.indexOf('=');

        String colExpr = trimWhitespace(expr.substring(0, indexOf));
        String paramExpr = trimWhitespace(expr.substring(indexOf + 1));

        boolean isSupportIFNULL = Boolean.TRUE.equals(dao.isSupportFunction("IFNULL"));

        //如果是数字
        if (Number.class.isAssignableFrom(dbColumnType)) {
            //
            //SQL 条件语句 (IF, CASE WHEN, IFNULL)
            // // Case表达式是SQL标准（SQL92发行版）的一部分，并已在Oracle Database、SQL Server、 MySQL、 PostgreSQL、 IBM UDB和其他数据库服务器中实现；
            // 使用加号
            if (updateOp.convertNullValueForIncrementMode()) {

                if (isSupportIFNULL) {
                    //语句简化
                    expr = colExpr + " = IFNULL(" + colExpr + " , 0) + IFNULL(" + paramExpr + " , 0)";
                } else {
                    expr = colExpr + " = (" + new Case().when(colExpr + " IS NULL ", "0").elseExpr(colExpr)
                            + ") + (" + new Case().when(paramExpr + " IS NULL ", "0").elseExpr(paramExpr) + ")";

                    //双份的参数
                    holder.value = new Object[]{holder.value, holder.value};
                }

            } else {
                expr = colExpr + " = (" + colExpr + " + " + paramExpr + ")";
            }

        } else if (CharSequence.class.isAssignableFrom(dbColumnType)) {

            //字符串相加 使用 CONCAT 函数

            // expr = colExpr + " = CONCAT( IFNULL(" + colExpr + ",'') , IFNULL(" + paramExpr + ",'') )";

            if (updateOp.convertNullValueForIncrementMode()) {

                if (isSupportIFNULL) {
                    expr = colExpr + " = CONCAT( IFNULL(" + colExpr + " , '') , IFNULL(" + paramExpr + " , ''))";
                } else {
                    expr = colExpr + " = CONCAT(" + new Case().when(colExpr + " IS NULL ", "''").elseExpr(colExpr)
                            + " , " + new Case().when(paramExpr + " IS NULL ", "''").elseExpr(paramExpr) + ")";
                    //双份的参数
                    holder.value = new Object[]{holder.value, holder.value};
                }

            } else {
                expr = colExpr + " = CONCAT(" + colExpr + " , " + paramExpr + ")";
            }

        } else {
            throw exSupplier.get();
        }

        return expr;
    }

}
