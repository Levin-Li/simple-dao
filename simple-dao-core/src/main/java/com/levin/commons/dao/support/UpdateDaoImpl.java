package com.levin.commons.dao.support;


import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NonUniqueResultException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

    /**
     * 设置更新字段
     *
     * @param isAppend                             是否加入表达式，方便链式调
     * @param incrementMode                        是否增量模式
     * @param autoConvertNullValueForIncrementMode 增量模式时，是否自动转换空值
     * @param entityAttrName                       需要更新的属性名，会自动尝试加上别名
     * @param paramValue                           参数值
     * @return
     */
    @Override
    public UpdateDao<T> set(Boolean isAppend, boolean incrementMode, boolean autoConvertNullValueForIncrementMode, String entityAttrName, Object paramValue) {

        if (!Boolean.TRUE.equals(isAppend) && !hasText(entityAttrName)) {
            return this;
        }

        String expr = aroundColumnPrefix(entityAttrName) + " = " + getParamPlaceholder();

        //是否增量模式
        if (incrementMode) {

            ValueHolder<Object> holder = new ValueHolder<>(paramValue);

            expr = genIncrementExpr(autoConvertNullValueForIncrementMode, entityAttrName, null, expr, holder);

            //参数
            paramValue = holder.value;
        }

        append(expr, paramValue);

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

        try {
            dao.setCurrentThreadMaxLimit(getSafeModeMaxLimit());
            return dao.update(isNative(), rowStart, rowCount, statement, genFinalParamList());
        } finally {
            dao.setCurrentThreadMaxLimit(null);
        }
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
                            && expr.contains("=")) {

                        expr = genIncrementExpr(updateOp.convertNullValueForIncrementMode(), name, varType, expr, holder);
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
    protected String genIncrementExpr(boolean convertNullValueForIncrementMode, String name, Class<?> varType, String expr, ValueHolder<Object> holder) {

        //数据库字段的类型，必须存在更新的对象
        final Class<?> dbColumnType = QueryAnnotationUtil.getFieldType(entityClass, name);

        final Supplier<StatementBuildException> exSupplier = () -> new StatementBuildException("Increment update can't support type[" + dbColumnType + "]，Only Number or String");

        Assert.notNull(dbColumnType, exSupplier);

        //检查是否非法的语句
        //Assert.isTrue(expr.charAt(0) != '(',() -> new StatementBuildException("非法的语句"));

        int indexOf = expr.indexOf('=');

        final String colExpr = ExprUtils.trimParenthesesPair(expr.substring(0, indexOf));
        final String paramExpr = ExprUtils.trimParenthesesPair(expr.substring(indexOf + 1));

        //是否支持 IFNULL 函数
        final boolean isSupportIFNULL = Boolean.TRUE.equals(getDao().isSupportFunction("IFNULL"));

        //生成语句
        final BiFunction<String, String, String> genFunc = (fun, defaultValue) -> {

            String tempExpr = "";

            String delim = hasText(fun) ? " , " : " + ";

            if (convertNullValueForIncrementMode) {

                //SQL 条件语句 (IF, CASE WHEN, IFNULL)
                // // Case表达式是SQL标准（SQL92发行版）的一部分，并已在Oracle Database、SQL Server、 MySQL、 PostgreSQL、 IBM UDB和其他数据库服务器中实现；

                if (isSupportIFNULL) {
                    //IFNULL 简化语句
                    tempExpr = colExpr + " = " + fun + "( IFNULL(" + colExpr + " , " + defaultValue + ") " + delim + " IFNULL(" + paramExpr + " , " + defaultValue + ") )";
                } else {
                    tempExpr = colExpr + " = " + fun + "( (" + new Case().when(colExpr + " IS NULL ", defaultValue).elseExpr(colExpr)
                            + ") " + delim + " (" + new Case().when(paramExpr + " IS NULL ", defaultValue).elseExpr(paramExpr) + ") )";
                    //双份的参数
                    holder.value = new Object[]{holder.value, holder.value};
                }

            } else {
                //QL:
                tempExpr = colExpr + " = " + fun + "(" + colExpr + delim + paramExpr + ")";
            }

            return tempExpr;
        };


        //如果是数字
        if (Number.class.isAssignableFrom(dbColumnType)) {
            expr = genFunc.apply("", "0");
        } else if (CharSequence.class.isAssignableFrom(dbColumnType)) {
            //字符串相加 使用 CONCAT 函数
            expr = genFunc.apply("CONCAT", "''");
        } else {
            throw exSupplier.get();
        }

        return expr;
    }

}
