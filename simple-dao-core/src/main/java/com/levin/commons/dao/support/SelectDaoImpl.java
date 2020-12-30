package com.levin.commons.dao.support;


import com.levin.commons.dao.Converter;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.SelectDao;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.annotation.E_C;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.repository.annotation.QueryRequest;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QLUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import com.levin.commons.utils.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.levin.commons.dao.util.ExprUtils.getExprForJpaJoinFetch;
import static org.springframework.util.StringUtils.hasText;

/**
 * 查询Dao实现类
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 */
public class SelectDaoImpl<T>
        extends ConditionBuilderImpl<T, SelectDao<T>>
        implements SelectDao<T> {

    private static final String SELECT_PACKAGE_NAME = Select.class.getPackage().getName();

    transient MiniDao dao;

    //选择
    final SimpleList<String> selectColumns = new SimpleList<>(true, new ArrayList(5), DELIMITER);

    final Map<String, Object[/* 实体属性名，DTO 对象字段或方法 */]> selectColumnsMap = new LinkedHashMap<>(7);

    final List selectParamValues = new ArrayList(7);

    //GroupBy 自动忽略重复字符
    final SimpleList<String> groupByColumns = new SimpleList<>(false, new ArrayList(5), DELIMITER);

    final List groupByParamValues = new ArrayList(5);

    private ExprNode havingExprRootNode = new ExprNode(AND.class.getSimpleName(), true);

    final List havingParamValues = new ArrayList(5);

    final SimpleList<OrderByObj> orderByColumns = new SimpleList<>(false, new ArrayList<OrderByObj>(5), DELIMITER);

    final StringBuilder joinStatement = new StringBuilder();

    final Map<String, String> fetchAttrs = new LinkedHashMap<>();

    //默认的排序
    final StringBuilder defaultOrderByStatement = new StringBuilder();

    //自己定义表达式
    String fromStatement;

    boolean hasStatColumns = false;


    Class resultType;


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

        if (!hasText(fromStatement)) {
            throw new IllegalArgumentException("fromStatement is null");
        }

    }

    public SelectDaoImpl(MiniDao dao, String tableName, String alias) {
        super(tableName, alias);
        this.dao = dao;
    }

    public SelectDaoImpl(MiniDao dao, Class<T> entityClass, String alias) {
        super(entityClass, alias);
        this.dao = dao;
    }


    @Override
    protected MiniDao getDao() {
        return dao;
    }

    @Override
    protected void setFromStatement(String fromStatement) {
        //没有内容
        if (!hasText(this.fromStatement)
                && entityClass == null
                && !hasText(this.tableName)) {
            this.fromStatement = fromStatement;
        }
    }


    @Override
    public SelectDao<T> select(String expr, Object... paramValues) {

        return select(true, expr, paramValues);
    }

    @Override
    public SelectDao<T> select(Boolean isAppend, String expr, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && selectColumns.add(expr)) {
            selectParamValues.add(paramValues);
        }

        return this;
    }

    @Override
    public SelectDao<T> select(String... columnNames) {

        if (columnNames != null) {
            for (String columnName : columnNames) {
                if (hasText(columnName)) {
                    selectColumns.add(aroundColumnPrefix(columnName));
                }
            }
        }

        return this;
    }

    @Override
    public String getAlias() {
        return alias;
    }


    public SelectDaoImpl<T> setQueryRequest(QueryRequest queryRequest) {

        if (queryRequest == null)
            return this;

        if (!hasText(this.fromStatement)
                && hasText(queryRequest.fromStatement())) {
            this.fromStatement = queryRequest.fromStatement();
        }

        //增加选择字段
        select(queryRequest.selectStatement());

        //增加连接语句
        join(queryRequest.joinStatement());

        //增加抓取的子集合
        joinFetch(Fetch.JoinType.Left, queryRequest.joinFetchSetAttrs());

        //设置默认的排序语句
        if (hasText(queryRequest.defaultOrderBy())) {
            defaultOrderByStatement.append(queryRequest.defaultOrderBy());
        }

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
    public SelectDao<T> join(String... joinStatements) {

        if (joinStatements != null) {
            for (String statement : joinStatements) {
                if (hasText(statement)) {
                    this.joinStatement.append(" ").append(statement).append(" ");
                }
            }
        }

        return this;
    }

    @Override
    public SelectDao<T> join(Boolean isAppend, String... joinStatements) {

        if (Boolean.TRUE.equals(isAppend)) {
            join(joinStatements);
        }

        return this;
    }

    @Override
    public SelectDao<T> joinFetch(String... setAttrs) {
        return joinFetch(Fetch.JoinType.Left, setAttrs);
    }

    @Override
    public SelectDao<T> joinFetch(Boolean isAppend, String... setAttrs) {

        if (Boolean.TRUE.equals(isAppend)) {
            joinFetch(setAttrs);
        }

        return this;
    }

    @Override
    public SelectDao<T> joinFetch(Fetch.JoinType joinType, String... setAttrs) {
        return joinFetch(null, joinType, setAttrs);
    }

    protected SelectDao<T> joinFetch(String domain, Fetch.JoinType joinType, String... setAttrs) {

        //仅对 JPA dao 有效
        if ((dao != null && !dao.isJpa()) || setAttrs == null || setAttrs.length < 1) {
            return this;
        }

        if (joinType == null) {
            joinType = Fetch.JoinType.Inner;
        }

        for (String setAttr : setAttrs) {

            if (!hasText(setAttr)) {
                continue;
            }

            setAttr = getExprForJpaJoinFetch(entityClass, getAlias(), setAttr);


//            //如果没有使用别名，尝试使用别名
//            if (!setAttr.contains(".")) {
//
//                if (!hasText(this.alias)) {
//                    throw new StatementBuildException("join fetch  attr [" + setAttr + "] must be set alias");
//                }
//
//                setAttr = aroundColumnPrefix(domain, setAttr);
//            }

            setAttr = aroundColumnPrefix(domain, setAttr);

            fetchAttrs.put(setAttr, (joinType == null ? "" : joinType.name()) + " Join Fetch " + setAttr);

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

        if (columns != null) {
            for (String column : columns) {
                if (hasText(column)) {
                    groupByColumns.add(aroundColumnPrefix(column));
                }
            }
        }

        return this;
    }

    @Override
    public SelectDao<T> groupBy(String expr, Object... paramValues) {
        return groupBy(true, expr, paramValues);
    }

    @Override
    public SelectDao<T> groupBy(Boolean isAppend, String expr, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && hasText(expr)) {
            groupByColumns.add(expr);
            groupByParamValues.add(paramValues);
        }

        return this;
    }

    @Override
    public SelectDao<T> having(String havingStatement, Object... paramValues) {

        return having(true, havingStatement, paramValues);
    }

    @Override
    public SelectDao<T> having(Boolean isAppend, String havingStatement, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && hasText(havingStatement)
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
    public SelectDao<T> orderBy(String... columnNames) {
        return orderBy(true, columnNames);
    }

    @Override
    public SelectDao<T> orderBy(Boolean isAppend, String... columnNames) {

        if (Boolean.TRUE.equals(isAppend)
                && columnNames != null) {
            for (String column : columnNames) {
                if (hasText(column)) {
                    orderByColumns.add(new OrderByObj(column));
                }
            }
        }

        return this;
    }

    protected void addOrderBy(String expr, int index, OrderBy.Type type) {
        orderByColumns.add(new OrderByObj(index, expr, type));
    }

    /**
     * 增加排序字段
     *
     * @param type        如果不填写，默认为 Desc
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    @Override
    public SelectDao<T> orderBy(OrderBy.Type type, String... columnNames) {

        if (columnNames == null || columnNames.length == 0) {
            return this;
        }

        //自动增加别名
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = aroundColumnPrefix(columnNames[i]);
        }

        if (type == null) {
            type = OrderBy.Type.Desc;
        }

        //加上排序方式
        for (int i = 0; i < columnNames.length; i++) {

            if (hasText(columnNames[i])) {
                columnNames[i] = columnNames[i] + " " + type.name();
            }

        }

        orderBy(columnNames);

        return this;
    }

    @Override
    public void processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {


        //处理SelectColumn注解
        processSelectAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation, null);

        //同时处理GroupBy和Having子句
        processStatAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation, null);

        //处理排序注解
        processOrderByAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

        //处理抓取
        processFetchSetByAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);


        super.processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

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

        if ((opAnnotation instanceof OrderBy)) {
            OrderBy orderBy = (OrderBy) opAnnotation;
            orderByColumns.add(new OrderByObj(orderBy.order(), aroundColumnPrefix(orderBy.domain(), name), orderBy.type()));
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

        if ((opAnnotation instanceof Fetch)) {

            Fetch fetch = (Fetch) opAnnotation;

            //增加集合抓取
            joinFetch(fetch.domain(), fetch.joinType(), fetch.value());
            joinFetch(fetch.domain(), fetch.joinType(), fetch.attrs());

        }

    }

    protected void tryAppendHaving(Annotation opAnnotation, String expr, ValueHolder<? extends Object> holder, Object opParamValue) {

        Op op = ClassUtils.getValue(opAnnotation, "havingOp", false);

        Boolean not = ClassUtils.getValue(opAnnotation, E_C.not, false);

        if (op == null || Op.None.name().equals(op.name())) {
            return;
        }

        expr = op.gen(expr, getParamPlaceholder());

        if (Boolean.TRUE.equals(not)) {
            expr = " NOT(" + expr + ") ";
        }

        if (holder != null) {
            having(expr, holder.value, opParamValue);
        } else {
            having(expr, opParamValue);
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
    protected void processSelectAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation, String alias) {


        if (isPackageStartsWith(SELECT_PACKAGE_NAME, opAnnotation)) {

            genExprAndProcess(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation, (expr, holder) -> {


                tryAppendHaving(opAnnotation, expr, holder, value);

                expr = tryAppendAlias(expr, opAnnotation, alias);

                select(tryAppendDistinct(expr, opAnnotation), holder.value);

                //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句
                appendColumnMap(expr, fieldOrMethod, name);
            });

        }
    }


    String tryAppendDistinct(String expr, Annotation opAnnotation) {

        Boolean isDistinct = ClassUtils.getValue(opAnnotation, "isDistinct", false);

        return Boolean.TRUE.equals(isDistinct) ? (" DISTINCT(" + expr + ") ") : expr;
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
    protected void processStatAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation, String alias) {

        //如果不是同个包，或是 opAnnotation 为 null
        if (!QueryAnnotationUtil.isSamePackage(opAnnotation, GroupBy.class)) {
            return;
        }

        hasStatColumns = true;


        genExprAndProcess(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation, (expr, holder) -> {

            boolean isGroupBy = opAnnotation instanceof GroupBy;

            final String oldExpr = expr;

            String newAlias = hasText(alias) ? alias : ClassUtils.getValue(opAnnotation, "alias", false);

            boolean hasAlias = hasText(newAlias);

            if (!hasAlias) {
                newAlias = expr;
            } else {
                expr = expr + " AS " + newAlias;
            }

            if (isGroupBy) {
                //增加GroupBy字段
                if (hasAlias) {
                    groupBy(true, newAlias);
                } else {
                    groupBy(oldExpr, holder.value);
                }
            }

            //JPA having 字句 不支持别名
            boolean useAlias = isNative() && hasAlias;

            tryAppendHaving(opAnnotation, useAlias ? newAlias : oldExpr, useAlias ? null : holder, value);

            //增加 Order By
            tryAppendOrderBy(useAlias ? newAlias : oldExpr, opAnnotation);

            select(expr, holder.value);

            //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句
            appendColumnMap(expr, fieldOrMethod, name);

        });

    }

    private void tryAppendOrderBy(String expr, Annotation opAnnotation) {

        OrderBy[] orderByList = ClassUtils.getValue(opAnnotation, "orderBy", false);

        appendOrderBy(expr, orderByList);

    }

    protected SelectDao<T> appendOrderBy(String expr, OrderBy... orderByList) {

        if (orderByList != null) {

            for (int i = 0; i < orderByList.length; i++) {

                OrderBy orderBy = orderByList[i];

                if (orderBy == null) {
                    continue;
                }

                expr = hasText(orderBy.value()) ? aroundColumnPrefix(orderBy.domain(), orderBy.value()) : expr;

                if (hasText(expr)) {
                    addOrderBy(expr, orderBy.order(), orderBy.type());
                }
            }
        }

        return this;
    }


    private String tryAppendAlias(String expr, Annotation opAnnotation, String alias) {

        if (!hasText(alias)) {
            alias = ClassUtils.getValue(opAnnotation, "alias", false);
        }

        return hasText(alias) ? expr + " AS " + alias + " " : expr;
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

        if (hasText(fromStatement)) {
            String from = getText(fromStatement, "").trim();
            boolean hasKey = from.toLowerCase().startsWith("from ");
            return (hasKey ? " " + fromStatement : " From " + fromStatement) + getText(joinStatement.toString(), " ");
        } else {
            return super.genFromStatement() + getText(joinStatement.toString(), " ");
        }

    }

    /**
     * 生成统计记录的语句，生成的语句会排除排序
     *
     * @param isCountQueryResult 是否用于统计总数
     * @return
     */
    String genQL(boolean isCountQueryResult) {

        StringBuilder builder = new StringBuilder();

        //目前如果没有要选择字段，不会加入select 子句
        if (!isCountQueryResult && selectColumns.length() > 0) {
            builder.insert(0, "Select " + selectColumns);
        } else if (isNative()) {
            builder.insert(0, "Select * ");
        } else if (!isCountQueryResult && fetchAttrs.size() > 0) {
            // builder.insert(0, "Select DISTINCT(" + getText(getAlias(), "")+")");
        }

        String genFromStatement = genFromStatement();

        if (!hasContent(genFromStatement)) {
            throw new IllegalArgumentException("from statement not set");
        }

        builder.append(" ").append(genFromStatement);


        //如果不是统计语句，则允许集合抓取语句
        if (!isCountQueryResult && fetchAttrs.size() > 0) {
            fetchAttrs.values().forEach(v -> builder.append(" ").append(v).append(" "));
        }

        String whereStatement = genWhereStatement();

        builder.append(" ").append(whereStatement);

        //如果GroupBy子句有内容，Having子句才有效，则否Having中的条件将被忽略
        if (groupByColumns.length() > 0) {

            builder.append(" Group By  " + groupByColumns);

            String havingStatement = havingExprRootNode.toString();

            if (havingStatement.length() > 0) {
                builder.append(" Having  " + havingStatement);
            }

        } else if (havingExprRootNode.toString().length() > 0) {
            throw new StatementBuildException("found 'having' statement but not found 'group by' statement");
        }

        //如果只是统计总数，则不把排序语句加入，可以提升速度
        if (!isCountQueryResult) {
            //以下代理是处理排序语句
            if (orderByColumns.length() > 0) {
                //排序
                Collections.sort(orderByColumns.getList());
                builder.append(" Order By  " + orderByColumns);
            } else if (defaultOrderByStatement.length() > 0) {
                //加入默认排序语句
                builder.append(" Order By  " + defaultOrderByStatement);
            }
        }

        if (this.isSafeMode() && !hasText(whereStatement) && !hasLimit()) {
            throw new StatementBuildException("safe mode not allow no where statement or not limit SQL[" + builder + "]");
        }

        return ExprUtils.replace(builder.toString(), getDaoContextValues());
    }

    @Override
    public long count() {

        if (isNative()) {
            return count("Select Count(*) From (" + this.genFinalStatement() + ") AS cnt_tmp", genFinalParamList());
        }

        //JPA 暂时不支持对统计查询进行二次统计
        if (hasStatColumns()) {
            throw new StatementBuildException("JPA暂时不支持对统计查询进行二次统计，请使用原生查询");
        }

        String column = "1";

        //如果有指定的查询字段
        if (selectColumns.length() > 0) {
            //@todo 待修复一个已知bug-201709262350，返回类型为 Long ，注意 count(*) 语法在 hibernate 中可用，但在 toplink 其它产品中并不可用

            // column = foundColumn(column, selectColumns.toString());
            column = "1";

        } else if (hasText(alias)) {
            //如果没有具体的查询字段，则可以用别名进行统计
            column = alias;
        }


        return count("Select Count(" + column + ") " + genQL(true), getDaoContextValues(), whereParamValues, havingParamValues);

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

        if (list.isEmpty() || list.get(0) == null) {
            return 0;
        }

        return list.get(0).longValue();
    }


    @Override
    public String genFinalStatement() {
        return genQL(false);
    }

    @Override
    public List genFinalParamList() {
        return QueryAnnotationUtil.flattenParams(null
                , getDaoContextValues()
                , selectParamValues
                , whereParamValues
                , groupByParamValues
                , havingParamValues);
    }

    /**
     * 获取结果集
     *
     * @return
     */
    @Override
    public <E> List<E> find() {
        return findForResultClass(null);
    }

    /**
     * 获取结果集
     *
     * @return
     */
//    @Override
    public <E> List<E> findForResultClass(Class<E> resultClass) {


        return (List<E>) dao.find(isNative(), resultClass, rowStart, rowCount, genFinalStatement(), genFinalParamList());
    }

/////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public <I, E> List<E> find(Converter<I, E> converter) {
        return findListAndConvert(converter);
    }

    //    @Override
    public <O> List<O> find(Function<? super Object, O> converter) {
        return findListAndConvert(converter);
    }


    public <E> List<E> findListAndConvert(Object converter) {

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

        if (targetType == null) {
            return find();
        }

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

        if (targetType == null) {
            return find();
        }

//        if (targetType == null) {
//            throw new IllegalArgumentException("targetType is null");
//        }

        autoSetFetch(targetType);

        // //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句

        if (selectColumnsMap.size() == 0) {

        }

        List<E> queryResultList = this.findForResultClass(null);

        if (queryResultList == null || queryResultList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        //如果是已经需要的类型
        if (targetType.isInstance(queryResultList.get(0))) {
            return queryResultList;
        }

        List<E> returnList = new ArrayList<>(queryResultList.size());

        ValueHolder<List<List<String>>> valueHolder = new ValueHolder<>(null);

        for (Object data : queryResultList) {

            if (this.selectColumnsMap.size() > 0
                    && data != null
                    && !data.getClass().isArray()) {
                data = new Object[]{data};
            }

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

        if (targetType == null) {
            return findOne();
        }

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

        if (targetType == null) {
            return findOne();
        }

        autoSetFetch(targetType);

        //尝试自动转换
        Object data = tryConvert2Map(findOne(), null);

        if (data == null || targetType.isInstance(data)) {
            return (E) data;
        }

        return ObjectUtil.copy(data, targetType, maxCopyDeep, ignoreProperties);

    }


    /**
     * 目的是防止 N + 1 查询
     *
     * @param targetType
     */
    public void autoSetFetch(Class targetType) {

        //如果不是 jpa 或是 没有指定实体类
        if (!getDao().isJpa() || targetType == null) {
            return;
        }

        this.resultType = targetType;


        ReflectionUtils.doWithFields(targetType, field -> {

                    Fetch fetch = field.getAnnotation(Fetch.class);

                    //如果有条件，并且条件不成功
                    if (StringUtils.hasText(fetch.condition())
                            && !Boolean.TRUE.equals(evalExpr(null, null, null, fetch.condition()))) {
                        return;
                    }

                    String property = fetch.value();

                    if (!hasText(property)) {
                        property = field.getName();
                    }

                    joinFetch(fetch.domain(), fetch.joinType(), property);

                }, field -> field.getAnnotation(Fetch.class) != null
        );


    }


    @Override
    public <I, E> E findOne(Converter<I, E> converter) {

        if (converter == null) {
            throw new IllegalArgumentException("converter is null");
        }

        Object data = findOne();

        return data != null ? converter.convert((I) data) : null;
    }


    //    @Override
    public <E> E findOne(Function<? super Object, E> converter) {

        if (converter == null) {
            throw new IllegalArgumentException("converter is null");
        }

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
    public Object tryConvert2Map(Object data, ValueHolder<List<List<String>>> valueHolder) {

        if (data == null || !data.getClass().isArray()) {
            return data;
        }

        final int arrayLen = Array.getLength(data);


        if (valueHolder == null) {
            valueHolder = new ValueHolder<>(null);
        }


        if (valueHolder.value == null) {
            valueHolder.value = getAliases(arrayLen);
        }


        //如果已经缓存字段对应关系，优化性能
        Map<String, Object> dataMap = new LinkedHashMap<>(selectColumns.size());

        for (int i = 0; i < arrayLen; i++) {

            Object value = Array.get(data, i);

            for (String key : valueHolder.value.get(i)) {
                dataMap.put(key, value);
            }

        }

        return dataMap;

    }

    private List<List<String>> getAliases(int arrayLen) {

        List<String[]> selectColumns = QLUtils.parseSelectColumns(null, this.selectColumns.toString());

        //如果数组长度
        if (arrayLen != selectColumns.size()) {
            return Collections.emptyList();
        }

        List<List<String>> columnNames = new ArrayList<>(arrayLen);

        for (String[] selectColumn : selectColumns) {

            List<String> aliases = new ArrayList<>(selectColumn.length);

            //列明对应关系
            columnNames.add(aliases);


            String expr = selectColumn[0];

            String alias = selectColumn[1];

            Object[] keys = selectColumnsMap.get(expr);


            String fieldName = (keys != null) ? (String) keys[0] : null;

            Object fieldOrMethod = (keys != null) ? keys[1] : null;

            if (fieldOrMethod != null) {

                String key = null;

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


                if (hasText(key)) {
                    aliases.add(key);
                }

                if (hasText(alias)) {
                    aliases.add(alias);
                }

            } else if (hasText(alias)) {
                aliases.add(alias);
            } else if (hasText(fieldName)) {
                aliases.add(fieldName);
            } else {
                aliases.add(expr);
            }


        }


        return columnNames;
    }


    private boolean isNameExists(String name, List<List<String>> names) {

        for (List<String> list : names) {
            if (list.contains(name)) {
                return true;
            }
        }

        return false;
    }

    private String getPropertyName(String key, Object[] keys) {

        if (!hasText(key)) {
            return key;
        }

        if (keys == null) {
            return key;
        }

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

        } else if (hasText(entityAttrName)) {
            key = entityAttrName;
        }

        return key;

    }
//////////////////////////////////////////////

    private SelectDao<T> processStat(int callMethodDeep, String expr, String alias, Object... paramValues) {

        if (!hasText(expr)) {
            throw new IllegalArgumentException("expr has no content");
        }

        String name = new Exception().getStackTrace()[callMethodDeep].getMethodName();

        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        Annotation annotation = QueryAnnotationUtil.getAllAnnotations().get(name);

        if (annotation == null) {
            throw new IllegalArgumentException("Annotation " + name + " not found");
        }

        processStatAnno(null, null, new Annotation[]{annotation}, expr, null, paramValues, annotation, alias);

        return this;
    }

    @Override
    public SelectDao<T> count(String expr, String alias) {
        return processStat(2, expr, alias);
    }

    @Override
    public SelectDao<T> avg(String expr, String alias, Map<String, Object>... paramValues) {
        return processStat(2, expr, alias, paramValues);
    }

    @Override
    public SelectDao<T> sum(String expr, String alias, Map<String, Object>... paramValues) {
        return processStat(2, expr, alias, paramValues);
    }

    @Override
    public SelectDao<T> max(String expr, String alias, Map<String, Object>... paramValues) {
        return processStat(2, expr, alias, paramValues);
    }

    @Override
    public SelectDao<T> min(String expr, String alias, Map<String, Object>... paramValues) {
        return processStat(2, expr, alias, paramValues);
    }

    @Override
    public SelectDao<T> groupByAsAnno(String expr, String alias, Map<String, Object>... paramValues) {

        Annotation annotation = QueryAnnotationUtil.getAnnotation(GroupBy.class);

        processStatAnno(null, null, new Annotation[]{annotation}, expr, null, paramValues, annotation, alias);

        return this;
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
            if (obj == this) {
                return true;
            }

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
