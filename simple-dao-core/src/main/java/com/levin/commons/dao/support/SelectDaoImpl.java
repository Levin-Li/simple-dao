package com.levin.commons.dao.support;


import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.levin.commons.dao.Converter;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.SelectDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.annotation.Not;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.SelectColumn;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Having;
import com.levin.commons.dao.simple.annotation.QueryRequest;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QLUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class SelectDaoImpl<T>
        extends ConditionBuilderImpl<T, SelectDao<T>>
        implements SelectDao<T> {

    private static final Logger logger = LoggerFactory.getLogger(SelectDaoImpl.class);

    transient MiniDao dao;

    //选择
    final SimpleList<String> selectColumns = new SimpleList<>(true, new ArrayList(5), DELIMITER);

    final Map<String, Object[/* 实体属性名，DTO 对象字段或方法 */]> selectColumnsMap = new LinkedHashMap<>(7);

    final List selectParamValues = new ArrayList(7);


    //GroupBy 自动忽略重复字符
    final SimpleList<String> groupByColumns = new SimpleList<>(false, new ArrayList(5), DELIMITER);

    private ExprNode havingExprRootNode = new ExprNode(AND.class.getSimpleName(), true);

    final List havingParamValues = new ArrayList(5);

    final SimpleList<OrderByObj> orderByColumns = new SimpleList<>(false, new ArrayList<OrderByObj>(5), " , ");

    final StringBuilder joinStatement = new StringBuilder();

    final StringBuilder fetchStatement = new StringBuilder();

    //默认的排序
    final StringBuilder defaultOrderByStatement = new StringBuilder();

    //自己定义表达式
    String fromStatement;

    boolean hasStatColumns = false;


    {
        disableSafeMode();
    }

    public SelectDaoImpl() {
        this(null, true);
    }

    public SelectDaoImpl(MiniDao dao, boolean isNative) {
        super(isNative);
        this.dao = dao;
    }

    public SelectDaoImpl(MiniDao dao, boolean isNative, String fromStatement) {

        super(isNative);

        this.dao = dao;

        this.fromStatement = fromStatement;

        if (!StringUtils.hasText(fromStatement))
            throw new IllegalArgumentException("fromStatement is null");
    }

    public SelectDaoImpl(MiniDao dao, String tableName, String alias) {
        super(tableName, alias);
        this.dao = dao;
    }

    public SelectDaoImpl(MiniDao dao, Class<T> entityClass, String alias) {
        super(entityClass, alias);
        this.dao = dao;
    }
//
//    @Override
//    protected String getParamPlaceholder() {
//        return dao.getParamPlaceholder(isNative());
//    }

    @Override
    protected MiniDao getDao() {
        return dao;
    }

    @Override
    protected void setFromStatement(String fromStatement) {
        if (!StringUtils.hasText(this.fromStatement)
                && entityClass == null
                && !StringUtils.hasText(this.tableName)) {
            this.fromStatement = fromStatement;
        }
    }

    @Override
    public SelectDao<T> select(String columns, Object... paramValues) {

        selectColumns.clear();
        selectParamValues.clear();
        selectColumnsMap.clear();

        hasStatColumns = false;

        appendColumns(columns, paramValues);

        return this;
    }

    @Override
    public SelectDao<T> appendColumns(String columns, Object... paramValues) {

        if (selectColumns.add(columns)) {
            selectParamValues.add(paramValues);
        }

        return this;
    }

    @Override
    public SelectDao<T> appendSelectColumns(String columns, Object... paramValues) {

        return appendColumns(columns, paramValues);
    }


    @Override
    public String getAlias() {
        return alias;
    }


    public SelectDaoImpl<T> setQueryRequest(QueryRequest queryRequest) {

        if (queryRequest == null)
            return this;

        if (!StringUtils.hasText(this.fromStatement)
                && StringUtils.hasText(queryRequest.fromStatement()))
            this.fromStatement = queryRequest.fromStatement();

        //增加选择字段
        appendSelectColumns(queryRequest.selectStatement());

        //增加连接语句
        appendJoin(queryRequest.joinStatement());

        //增加抓取的子集合
        appendJoinFetchSet(true, queryRequest.joinFetchSetAttrs());

        //设置默认的排序语句
        setDefaultOrderByStatement(queryRequest.defaultOrderBy());

        return this;
    }


    @Override
    public boolean hasStatColumns() {
        return hasStatColumns || this.groupByColumns.size() > 0;
    }

    @Override
    public boolean hasSelectColumns() {
        return this.selectColumns.size() > 0;
    }


    @Override
    public SelectDao<T> join(String joinExpr) {

        joinStatement.setLength(0);

        return appendJoin(joinExpr);
    }

    @Override
    public SelectDao<T> appendJoin(String... joinStatements) {

        if (joinStatements != null) {
            for (String statement : joinStatements) {
                if (StringUtils.hasText(statement))
                    this.joinStatement.append(" ").append(statement).append(" ");
            }
        }

        return this;
    }

    @Override
    public SelectDao<T> joinFetchSet(boolean isLeftJoin, String... setAttrs) {

        fetchStatement.setLength(0);

        appendJoinFetchSet(isLeftJoin, setAttrs);

        return this;
    }

    @Override
    public SelectDao<T> appendJoinFetchSet(boolean isLeftJoin, String... setAttrs) {

        //仅对 JPA dao 有效
        if ((dao != null && !dao.isJpa()) || setAttrs == null || setAttrs.length < 1) {
            return this;
        }

        if (!StringUtils.hasText(this.alias)) {
            throw new StatementBuildException("left join must be set alias");
        }

        for (String setAttr : setAttrs) {

            if (!StringUtils.hasText(setAttr))
                continue;

            //如果没有使用别名，尝试使用别名
            if (!setAttr.contains("."))
                setAttr = aroundColumnPrefix(setAttr);

            fetchStatement.append(" ").append((isLeftJoin ? "left" : "inner") + " join fetch " + setAttr).append(" ");

        }


        return this;
    }

    /**
     * 设置group by
     *
     * @param columns
     * @return
     */
    @Override
    public SelectDao<T> groupBy(String... columns) {

        groupByColumns.clear();

        return appendGroupBy(columns);
    }

    /**
     * 设置group by
     *
     * @param columns
     * @return
     */
    @Override
    public SelectDao<T> appendGroupBy(String... columns) {

        if (columns != null) {
            for (String column : columns) {
                groupByColumns.add(column);
            }
        }

        return this;
    }

    @Override
    public SelectDao<T> having(String havingStatement, Object... paramValues) {

        //清除
        this.havingExprRootNode.clear();
        this.havingParamValues.clear();

        return appendHaving(havingStatement, paramValues);

    }

    @Override
    public SelectDao<T> appendHaving(String havingStatement, Object... paramValues) {

        if (StringUtils.hasText(havingStatement)
                && this.havingExprRootNode.addToCurrentNode(havingStatement)) {
            this.havingParamValues.add(paramValues);
        }

        return this;
    }

    @Override
    protected void beginLogic(String op, boolean valid) {

        super.beginLogic(op, valid);

        havingExprRootNode.beginGroup(op, valid);
    }

    @Override
    protected void endLogic() {

        super.endLogic();

        havingExprRootNode.endGroup();
    }

    @Override
    public SelectDao<T> orderBy(String... columns) {

        //清除条件
        orderByColumns.clear();

        appendOrderBy(columns);

        return this;
    }

    /**
     * 增加排序对象
     *
     * @param columns 例：  "name desc" , "createTime desc"
     * @return
     */
    @Override
    public SelectDao<T> appendOrderBy(String... columns) {

        if (columns == null)
            return this;

        for (String column : columns) {
            orderByColumns.add(new OrderByObj(column));
        }

        return this;
    }


    /**
     * 增加排序对象
     *
     * @param type
     * @param columnNames 例：  "name desc" , "createTime desc"
     * @return
     */
    @Override
    public SelectDao<T> appendOrderBy(OrderBy.Type type, String... columnNames) {

        if (columnNames == null || columnNames.length == 0)
            return this;

        //自动增加别名
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = aroundColumnPrefix(columnNames[i]);
        }

        if (type != null) {
            //加上排序方式
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = columnNames[i] + " " + type.name();
            }
        }

        appendOrderBy(columnNames);

        return this;
    }

    /**
     * 设置查询的分页
     *
     * @param pageIndex 第几页，从1开始
     * @param pageSize  分页大小
     * @return
     */
    @Override
    public SelectDao<T> page(int pageIndex, int pageSize) {
        this.rowStart = (pageIndex - 1) * pageSize;
        this.rowCount = pageSize;
        return this;
    }


    @Override
    public boolean processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        //如果包含Having注解，则把条件做为Having条件
        //如果是having子句
        Having having = QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, Having.class);

        //如果包括Having注解
        if (having != null) {

            Not notAnno = QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, Not.class);

            boolean complexType = !hasPrimitiveAnno(varAnnotations) && isComplexType(varType, value);

            if (!complexType) {
                // value = tryToConvertValue(name, value);
            }

            ValueHolder holder = new ValueHolder(bean, value);

            String conditionExpr = genConditionExpr(complexType, name, holder, opAnnotation);

            appendHaving(processNotTag(bean, notAnno, conditionExpr), holder.value);

        } else {
            super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);
        }

        //处理SelectColumn注解
        processSelectAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        //同时处理GroupBy和Having子句
        processStatAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        //处理排序注解
        processOrderByAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        //处理排
        processFetchSetByAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        return true;
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
    protected void processOrderByAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        OrderBy orderBy = QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, OrderBy.class);

        if (orderBy != null) {
            orderByColumns.add(new OrderByObj(orderBy.order(), orderBy.isAppendAliasPrefix() ? aroundColumnPrefix(name) : name, orderBy.type()));
        }

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
    protected void processFetchSetByAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        if (!(opAnnotation instanceof Fetch))
            return;

        Fetch fetch = (Fetch) opAnnotation;

        //增加集合抓取
        appendJoinFetchSet(fetch.isLeftJoin(), fetch.value());
        appendJoinFetchSet(fetch.isLeftJoin(), fetch.attrs());

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
    protected void processSelectAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        if (!(opAnnotation instanceof SelectColumn))
            return;

        SelectColumn anno = (SelectColumn) opAnnotation;

        //如果忽略空值
        if (anno.useVarValue()
                && anno.ignoreNullValue()
                && value == null)
            return;

        String expr = "";

        boolean isSubQuery = false;

        boolean complexType = !hasPrimitiveAnno(varAnnotations) && isComplexType(varType, value);


        ValueHolder holder = new ValueHolder(bean, value);

        //子查询
        if (StringUtils.hasText(anno.subQuery())) {

            isSubQuery = true;

            expr = anno.subQuery();

            expr = doReplace(expr, anno.useVarValue(), holder);

            value = holder.value;


        } else if (complexType) {
            //子查询
            isSubQuery = true;

            expr = buildSubQuery(holder);
            value = holder.value;
        } else {

            expr = anno.isAppendAliasPrefix() ? aroundColumnPrefix(name) : name;

            value = anno.useVarValue() ? value : new Object[0];
        }

        //如果是子查询加上 as
        if (isSubQuery) {
            expr = autoAroundParentheses(anno.prefix(), expr, anno.suffix()) + " AS " + name;
        } else {
            expr = anno.prefix() + expr + anno.suffix();
        }

        expr = " " + anno.op() + " " + expr;


        appendSelectColumns(expr, value);

        //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句
        appendColumnMap(expr, (StringUtils.hasText(anno.outputColumnName()) ? anno.outputColumnName() : fieldOrMethod), name);

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
    protected void processStatAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        //如果不是同个包，或是 opAnnotation 为 null
        if (!QueryAnnotationUtil.isSamePackage(opAnnotation, GroupBy.class))
            return;

        //平均数的参数要求是 double 型
        if (opAnnotation instanceof Avg) {

        }

        hasStatColumns = true;

        AnnotationModel model = AnnotationModel.copy(opAnnotation);

        String column = model.getOp() + model.getPrefix() + aroundColumnPrefix(name) + model.getSuffix();


        //@todo 增加别名
//        if (fieldOrMethod instanceof Field) {
//            column += " AS " +
//        }

        //增加选择字段
        appendSelectColumns(column);

        //字段映射
        appendColumnMap(column, fieldOrMethod, name);

        if (opAnnotation instanceof GroupBy) {
            //增加GroupBy字段

            appendGroupBy(column);
        }

        if (StringUtils.hasText(model.getHavingOp())) {
            appendHaving(column + " " + model.getHavingOp() + " " + getParamPlaceholder(), value);
        }
    }

    /**
     * 增加字段映射
     *
     * @param column
     * @param fieldOrMethod
     * @param name
     */
    protected void appendColumnMap(String column, Object fieldOrMethod, String name) {

/*        String name2 = null;

        if (fieldOrMethod instanceof Field) {
            name2 = ((Field) fieldOrMethod).getName();
        } else if (fieldOrMethod instanceof Method) {
            name2 = ((Method) fieldOrMethod).getName();
            if (name2.startsWith("get")) {
                name2 = "" + Character.toLowerCase(name2.charAt(3)) + name2.substring(4);
            }
        }*/

        selectColumnsMap.put(QLUtils.parseSelectColumn(column.trim()), new Object[]{name, fieldOrMethod});

    }

    @Override
    protected String genFromStatement() {

        if (StringUtils.hasText(fromStatement)) {
            String from = getText(fromStatement, "").trim();
            boolean hasKey = from.toLowerCase().startsWith("from ");
            return (hasKey ? " " + fromStatement : " From " + fromStatement) + getText(joinStatement.toString(), " ");
        } else
            return super.genFromStatement() + getText(joinStatement.toString(), " ");

    }

    /**
     * 生成统计记录的语句，生成的语句会排除排序
     *
     * @param isCountQueryResult 是否用于统计总数
     * @return
     */
    StringBuilder genQL(boolean isCountQueryResult) {

        StringBuilder builder = new StringBuilder();

        //目前如果没有要选择字段，不会加入select 子句
        if (!isCountQueryResult && selectColumns.length() > 0) {
            builder.insert(0, "Select " + selectColumns);
        } else if (isNative()) {
            builder.insert(0, "Select * ");
        }


        String genFromStatement = genFromStatement();

        if (!hasContent(genFromStatement))
            throw new IllegalArgumentException("from statement not set");

        builder.append(" ").append(genFromStatement);

        //如果不是统计语句，则允许集合抓取语句
        if (!isCountQueryResult && fetchStatement.length() > 0) {
            builder.append(" ").append(fetchStatement);
        }

        String whereStatement = genWhereStatement();

        builder.append(" ").append(whereStatement);

        //如果GroupBy子句有内容，Having子句才有效，则否Having中的条件将被忽略
        if (groupByColumns.length() > 0) {

            builder.append(" Group By  " + groupByColumns);

            String havingStatement = havingExprRootNode.toString();

            if (havingStatement.length() > 0)
                builder.append(" Having  " + havingStatement);

        } else if (havingExprRootNode.toString().length() > 0) {
            throw new StatementBuildException("found 'having' statement but not found 'group by' statement");
        }

        //如果只是统计总数，则不把排序语句加入
        if (isCountQueryResult)
            return builder;

        //以下代理是处理排序语句
        if (orderByColumns.length() > 0) {
            //排序
            Collections.sort(orderByColumns.getList());
            builder.append(" Order By  " + orderByColumns);
        } else if (defaultOrderByStatement.length() > 0) {
            //加入默认排序语句
            builder.append(" Order By  " + defaultOrderByStatement);
        }


        if (this.isSafeMode() && whereStatement.trim().length() == 0) {
            throw new StatementBuildException("safe mode not allow no where statement SQL[" + builder + "]");
        }


        return builder;
    }

    @Override
    public SelectDao<T> setDefaultOrderByStatement(String orderByStatement) {

        this.defaultOrderByStatement.setLength(0);

        if (orderByStatement != null && orderByStatement.trim().length() > 0)
            defaultOrderByStatement.append(orderByStatement);

        return this;
    }


    /**
     * 是否有查询的列
     *
     * @return
     */
    //@Override
    public boolean hasColumnsToQuery() {
        return selectColumns.length() > 0;
    }

    @Override
    public long count() {

        String column = "*";

        //如果有指定的查询字段
        if (selectColumns.length() > 0) {
            //@todo 待修复一个已知bug-201709262350，返回类型为 Long ，注意 count(*) 语法在 hibernate 中可用，但在 toplink 其它产品中并不可用

            //把整个查询做为子查询
            if (isNative())
                return count("Select Count('*') From (" + this.genFinalStatement() + ") AS cnt_tmp", genFinalParamList());

            column = foundColumn(column, selectColumns.toString());

        } else if (StringUtils.hasText(alias)) {
            //如果没有具体的查询字段，则可以用别名进行统计
            column = alias;
        }

        return count("Select Count(" + column + ") " + genQL(true), getDaoContextValues(), whereParamValues, havingParamValues);
    }

    public static String foundColumn(String defaultResult, String selectColumns) {

        try {
            SQLStatementParser parser = new SQLStatementParser("Select " + selectColumns + " From Test");

            SQLSelectQuery query = parser.createSQLSelectParser().query();

            if (query instanceof SQLSelectQueryBlock) {
                for (SQLSelectItem item : ((SQLSelectQueryBlock) query).getSelectList()) {
                    // System.out.println(":" + item.getAlias() + "," + item.getExpr() + "," + item.getExpr().getClass());
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        return item.getExpr().toString();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("SQL选择字段语句解析异常:" + selectColumns);
        }

        return defaultResult;

    }


    public static List<String> splitQL(String... selectColumns) {

        List<String> result = new ArrayList<>(7);

        try {
            SQLStatementParser parser = new SQLStatementParser("Select " + selectColumns + " From Test");

            SQLSelectQuery query = parser.createSQLSelectParser().query();

            if (query instanceof SQLSelectQueryBlock) {

                for (SQLSelectItem item : ((SQLSelectQueryBlock) query).getSelectList()) {

                    System.out.println(item.toString() + ":" + item.getAlias() + "," + item.getExpr() + "," + item.getExpr().getClass());

                    result.add(item.toString());

                }
            }
        } catch (Exception e) {
            logger.warn("SQL选择字段语句解析异常:" + selectColumns);
        }

        return result;
    }


    /**
     * 如果没有记录，或是记录为null值，都表示为0
     *
     * @param ql
     * @param paramValues
     * @return
     */
    private long count(String ql, Object... paramValues) {

        List<Number> list = dao.find(isNative(), null, -1, -1, ql, paramValues);

        if (list.isEmpty() || list.get(0) == null)
            return 0;

        return list.get(0).longValue();
    }


    @Override
    public String genFinalStatement() {
        return genQL(false).toString();
    }

    @Override
    public List genFinalParamList() {
        return QueryAnnotationUtil.flattenParams(null
                , getDaoContextValues()
                , selectParamValues
                , whereParamValues
                , havingParamValues);
    }

    /**
     * 获取结果集
     *
     * @return
     */
    @Override
    public <E> List<E> find() {
        return findForResultClass((Class<E>) null);
    }

    /**
     * 获取结果集
     *
     * @return
     */
//    @Override
    public <E> List<E> findForResultClass(Class<E> resultClass) {
        return (List<E>) dao.find(isNative(), resultClass, rowStart, rowCount, genQL(false).toString(), genFinalParamList());
    }

/////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param converter@return
     */
    @Override
    public <O> List<O> find(Converter<? super Object, O> converter) {
        return findAndConvert(converter);
    }

    @Override
    public <O> List<O> find(Function<? super Object, O> converter) {
        return findAndConvert(converter);
    }


    public <E> List<E> findAndConvert(Object converter) {

        if (converter == null)
            throw new IllegalArgumentException("converter is null");

        List<Object> queryResult = this.find();

        if (queryResult == null) {
            return Collections.EMPTY_LIST;
        }

        if (converter instanceof Converter) {
            return queryResult.stream().map(e -> ((Converter<Object, E>) converter).convert(e)).collect(Collectors.toList());
        } else if (converter instanceof Function) {
            return queryResult.stream().map((Function<Object, E>) converter).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("unknown converter type " + converter.getClass());
        }
    }


    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param targetType@return
     */
    @Override
    public <E> List<E> find(Class<E> targetType) {
        return find(targetType, 2);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param targetType
     * @return
     */
    @Override
    public <E> List<E> find(Class<E> targetType, int maxCopyDeep, String... ignoreProperties) {

        if (targetType == null)
            throw new IllegalArgumentException("targetType is null");

        // //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句

        // boolean isEntity = dao.isEntityType(targetType) || !dao.isJpa();

        List<E> queryResultList = this.findForResultClass(null);

        if (queryResultList == null || queryResultList.isEmpty())
            return new ArrayList<>(0);

        //如果是已经需要的类型
        if (targetType.isInstance(queryResultList.get(0)))
            return (List<E>) queryResultList;

        List<E> returnList = new ArrayList<>(queryResultList.size());

        ValueHolder<String[]> valueHolder = new ValueHolder<>(null);

        for (Object data : queryResultList) {

            //尝试自动转换成 Map
            data = tryConvert2Map(data, valueHolder);

            if (data == null || targetType.isInstance(data)) {
                returnList.add((E) data);
            } else {
                returnList.add(ObjectUtil.copy(data, targetType, maxCopyDeep, ignoreProperties));
            }

        }

        return returnList;
    }

    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public <E> E findOne() {
        //设置只取第一条
        setRowCount(1);

        List<E> list = find();

        return ((list != null && list.size() > 0) ? list.get(0) : null);
    }

    @Override
    public <E> E findOne(Class<E> targetType) {
        return findOne(targetType, 2);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param targetType
     * @return
     */
    @Override
    public <E> E findOne(Class<E> targetType, int maxCopyDeep, String... ignoreProperties) {

        //尝试自动转换
        Object data = tryConvert2Map(findOne(), null);

        if (data == null || targetType.isInstance(data))
            return (E) data;

        return ObjectUtil.copy(data, targetType, maxCopyDeep, ignoreProperties);

    }

    @Override
    public <O> O findOne(Converter<? super Object, O> converter) {

        if (converter == null)
            throw new IllegalArgumentException("converter is null");

        Object data = findOne();

        return data != null ? converter.convert(data) : null;
    }

    @Override
    public <E> E findOne(Function<? super Object, E> converter) {

        if (converter == null)
            throw new IllegalArgumentException("converter is null");

        Object data = findOne();

        return data != null ? converter.apply(data) : null;
    }


    @Override
    public <E> E copyProperties(Object source, E target, String... ignoreProperties) {
        return (E) ObjectUtil.copyProperties(source, target, -1, ignoreProperties);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public String removeAlias(String column) {

        if (alias != null) {

            String prefix = (alias.trim() + ".");

            ////  e.

            if (column.startsWith(prefix)) {
                column = column.substring(prefix.length());
            }

        }

        return column;
    }

    /**
     * 单查询返回值是数组时，尝试自动转换成 Map
     *
     * @param data
     * @param valueHolder 数据缓存
     * @return
     * @todo 优化性能，直接转换成对象
     */
    public Object tryConvert2Map(Object data, ValueHolder<String[]> valueHolder) {

        if (data == null || !data.getClass().isArray()) {
            return data;
        }

        final int arrayLen = Array.getLength(data);

        if (valueHolder != null
                && valueHolder.value != null
                && valueHolder.value.length == arrayLen) {

            //如果已经缓存字段对应关系，优化性能
            Map<String, Object> dataMap = new LinkedHashMap<>(selectColumns.size());

            for (int i = 0; i < arrayLen; i++) {
                dataMap.put(valueHolder.value[i], Array.get(data, i));
            }

            return dataMap;
        }


        List<String[]> selectColumns = QLUtils.parseSelectColumns(null, this.selectColumns.toString());

        //如果数组长度
        if (arrayLen != selectColumns.size()) {
            return data;
        }

        Map<String, Object> dataMap = new LinkedHashMap<>(selectColumns.size());

        //转化成 Map
        int idx = 0;

        String[] columnNames = new String[arrayLen];

        for (String[] selectColumn : selectColumns) {

            String key = selectColumn[selectColumn.length - 1];

            if (key == null) {
                key = selectColumn[0];
            }

            Object[] keys = selectColumnsMap.get(key);

            key = removeAlias(key);

            //
            if (keys != null && keys.length > 0) {

                //1、优先使用字段名
                Object fieldOrMethod = keys[1];

                //2、其次使用实体属性名
                String entityAttrName = (String) keys[0];

                if (fieldOrMethod != null) {

                    if (fieldOrMethod instanceof Field) {
                        key = ((Field) fieldOrMethod).getName();
                    } else if (fieldOrMethod instanceof Method) {
                        key = ((Method) fieldOrMethod).getName();

                        //去除 get
                        if (key.startsWith("get")) {
                            key = Character.toLowerCase(key.charAt(3)) + key.substring(4);
                        }
                    } else if (fieldOrMethod instanceof String) {
                        key = removeAlias((String) fieldOrMethod);
                    }

                } else if (StringUtils.hasText(entityAttrName)) {
                    key = entityAttrName;
                }

            }

            //列明对应关系
            columnNames[idx] = key;

            dataMap.put(key, Array.get(data, idx++));
        }


        if (valueHolder != null) {
            valueHolder.value = columnNames;
        }

        return dataMap;


    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    static class OrderByObj
            implements Comparable<OrderByObj> {

        int order = 0;
        String orderByStatement;
        OrderBy.Type type;

        public OrderByObj(String orderByStatement) {
            this.orderByStatement = orderByStatement;
        }

        public OrderByObj(int order, String orderByStatement, OrderBy.Type type) {
            this.order = order;
            this.orderByStatement = orderByStatement;
            this.type = type;
        }

        //按从小到大排序
        @Override
        public int compareTo(OrderByObj o) {
            //按从小到大排序
            return o.order - order;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj instanceof OrderByObj) {
                return orderByStatement.equals(((OrderByObj) obj).orderByStatement);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return orderByStatement.hashCode();
        }

        @Override
        public String toString() {
            return orderByStatement + (type != null ? " " + type.name() : "");
        }
    }

}
