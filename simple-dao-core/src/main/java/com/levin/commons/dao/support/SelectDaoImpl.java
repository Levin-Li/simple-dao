package com.levin.commons.dao.support;


import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.E_C;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.order.SimpleOrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.repository.annotation.QueryRequest;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QLUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.utils.ClassUtils;
import com.levin.commons.utils.MapUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //默认的排序，当没有排序的时候的默认排序
    final StringBuilder defaultOrderByStatement = new StringBuilder();

    //自己定义表达式
    String fromStatement;

    boolean hasStatColumns = false;

    Class resultType;


    final ContextHolder<String, Boolean> attrFetchList = ContextHolder.buildThreadContext(true);


    {
        disableSafeMode();
    }

    public SelectDaoImpl() {
        this(null, true);
    }

    public SelectDaoImpl(MiniDao dao, boolean isNative) {
        super(dao, isNative);

    }

    @Deprecated
    public SelectDaoImpl(MiniDao dao, boolean isNative, String fromStatement) {

        super(dao, isNative);


        this.fromStatement = fromStatement;

        if (!hasText(fromStatement)) {
            throw new IllegalArgumentException("fromStatement is null");
        }
    }

    public SelectDaoImpl(MiniDao dao, boolean isNative, String tableName, String alias) {
        super(dao, isNative, tableName, alias);
    }

    public SelectDaoImpl(MiniDao dao, boolean isNative, Class<T> entityClass, String alias) {
        super(dao, isNative, entityClass, alias);
    }


    @Override
    protected void setFromStatement(String fromStatement) {
        //没有内容
        if (!hasText(this.fromStatement)
                && !hasEntityClass()
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

        if (queryRequest == null) {
            return this;
        }

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

    protected void appendToAliasMap(String targetAlias, Class targetClass) {

        if (!ExprUtils.isValidClass(targetClass)) {
            throw new StatementBuildException("join class " + targetClass + " fail");
        }

        if (!targetClass.isAnnotationPresent(Entity.class)) {
            throw new StatementBuildException("join class " + targetClass.getName() + " not an entity class");
        }

        if (!hasText(targetAlias)) {
            throw new StatementBuildException("join class " + targetClass.getName() + " have to an alias ");
        }

        //转换成小写
//        targetAlias = targetAlias.trim().toLowerCase();

        if (aliasMap.containsKey(targetAlias)) {
            throw new StatementBuildException("join class " + targetClass.getName() + " alias " + targetAlias + " already use by " + aliasMap.get(targetAlias));
        }

        aliasMap.put(targetAlias, targetClass);

    }

    @Override
    public SelectDao<T> join(Boolean isAppend, Class targetClass, String targetAlias) {

        if (!Boolean.TRUE.equals(isAppend)) {
            return this;
        }

        appendToAliasMap(targetAlias, targetClass);


        String targetName = targetClass.getName();

        if (isNative()) {

            targetName = QueryAnnotationUtil.getTableNameByAnnotation(targetClass);

            targetName = getTableNameByPhysicalNamingStrategy(targetName);

        }

        //加入表达式
        join(" , " + targetName + " " + targetAlias + " ");

        return this;
    }

    @Override
    public SelectDao<T> join(Boolean isAppend, SimpleJoinOption... joinOptions) {

        if (Boolean.TRUE.equals(isAppend) && joinOptions != null) {

            Stream.of(joinOptions).filter(Objects::nonNull).forEachOrdered(
                    o -> join(true, o.entityClass(), o.alias())
            );

        }

        return this;
    }


    @Override
    public SelectDao<T> join(Boolean isAppend, JoinOption... joinOptions) {

        if (!Boolean.TRUE.equals(isAppend) || joinOptions == null) {
            return this;
        }

        String joinStatement = ExprUtils.genJoinStatement(getDao(), isNative()
                , this::appendToAliasMap
                , this::getTableNameByPhysicalNamingStrategy
                , this::getColumnNameByPhysicalNamingStrategy
                , entityClass, tableName, alias, joinOptions);

        if (hasText(joinStatement)) {
            join(true, joinStatement);
        }

        return this;
    }


    String fallbackAlias(String newAlias) {
        return StringUtils.hasText(newAlias) ? newAlias : this.alias;
    }

    /**
     * @param isAppend
     * @param entityClass
     * @param alias
     * @param joinColumn
     * @param joinTargetAlias
     * @param joinTargetColumn
     * @return
     */
    @Override
    public SelectDao<T> join(Boolean isAppend, Fetch.JoinType joinType, Class entityClass, String alias, String joinColumn, String joinTargetAlias, String joinTargetColumn) {

        if (joinType == null) {
            joinType = Fetch.JoinType.Left;
        }

        //保持注解一样
        joinColumn = hasText(joinColumn) ? joinColumn : "";
        joinTargetAlias = hasText(joinTargetAlias) ? joinTargetAlias : "";
        joinTargetColumn = hasText(joinTargetColumn) ? joinTargetColumn : "";

        Map<String, Object> map = MapUtils.putFirst(E_JoinOption.entityClass, entityClass)
                .put(E_JoinOption.type, joinType)
                .put(E_JoinOption.alias, alias)
                .put(E_JoinOption.joinColumn, joinColumn)
                .put(E_JoinOption.joinTargetAlias, fallbackAlias(joinTargetAlias))
                .put(E_JoinOption.joinTargetColumn, joinTargetColumn).build();

        return join(isAppend, (JoinOption) ClassUtils.newAnnotation(JoinOption.class, map));
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
        return joinFetch(false, null, joinType, setAttrs);
    }

    protected SelectDao<T> joinFetch(boolean isAppendToSelect, String domain, Fetch.JoinType joinType, String... setAttrs) {

        //仅对 JPA dao 有效
        if ((dao != null && !dao.isJpa()) || setAttrs == null || setAttrs.length < 1) {
            return this;
        }

        if (joinType == Fetch.JoinType.None) {
            return this;
        }

        if (joinType == null) {
            joinType = Fetch.JoinType.Inner;
        }

        for (String attr : setAttrs) {

            if (!hasText(attr)) {
                continue;
            }

            if (hasText(domain) && !domain.equalsIgnoreCase(getAlias())) {
                attr = getExprForJpaJoinFetch(aliasMap.get(domain), domain, attr);
            } else {
                attr = getExprForJpaJoinFetch(entityClass, getAlias(), attr);
            }

            //如果不是对象的属性
            if (!hasText(attr)) {
                continue;
            }

            attr = aroundColumnPrefix(domain, attr);

            //如果是相同的字段，会覆盖旧抓取属性
            fetchAttrs.put(attr, (joinType == null ? "" : joinType.name()) + " Join Fetch " + attr);

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
                && this.havingExprRootNode.currentNode().add(havingStatement)) {
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
        } else if ((opAnnotation instanceof SimpleOrderBy)) {
//            SimpleOrderBy orderBy = (SimpleOrderBy) opAnnotation;
            if (value instanceof String) {
                orderBy((String) value);
            } else if (value instanceof String[]) {
                orderBy((String[]) value);
            } else {
                throw new StatementBuildException("SimpleOrderBy注解必须注释在字符串或是字符串数组字段上");
            }

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

            if (fetch.isBindToField()
                    && fetch.joinType() != Fetch.JoinType.None
                    && fieldOrMethod instanceof Field) {

                String attrName = hasText(fetch.value()) ? fetch.value() : ((Field) fieldOrMethod).getName();

                joinFetch(true, fetch.domain(), fetch.joinType(), attrName);

                attrFetchList.put(((Field) fieldOrMethod).getDeclaringClass().getName() + "|" + attrName, true);

            }

            //增加集合抓取
            joinFetch(false, fetch.domain(), fetch.joinType(), fetch.attrs());

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


    private String tryAppendAlias(String expr, String alias) {
        return hasText(alias) ? " " + expr + " AS " + alias + " " : expr;
    }


    String tryAppendDistinctAndAlias(String expr, String alias, Annotation opAnnotation) {

        boolean isDistinct = Boolean.TRUE.equals(ClassUtils.getValue(opAnnotation, "isDistinct", false));

        expr = isDistinct ? (" DISTINCT(" + expr + ") ") : expr;

        if (isDistinct) {
            expr = hasText(alias) ? " " + expr + " AS " + alias + " " : expr;
        } else {
            expr = hasText(alias) ? " (" + expr + ") AS " + alias + " " : expr;
        }

        return expr;
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

                String newAlias = getAlias(fieldOrMethod, opAnnotation, alias);

                //  expr = tryAppendAlias(expr, newAlias);

                // ORDER BY 也不能使用别名

                tryAppendOrderBy(expr, newAlias, opAnnotation);

                expr = tryAppendDistinctAndAlias(expr, newAlias, opAnnotation);

                select(expr, holder.value);

                //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字典名称，只好通过 druid 解析 SQL 语句
                appendColumnMap(expr, newAlias, fieldOrMethod, name);

            });

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
    protected void processStatAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation, String alias) {

        //如果不是同个包，或是 opAnnotation 为 null
        if (!QueryAnnotationUtil.isSamePackage(opAnnotation, GroupBy.class)) {
            return;
        }

        hasStatColumns = true;

        genExprAndProcess(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation, (expr, holder) -> {

            boolean isGroupBy = opAnnotation instanceof GroupBy;

            final String oldExpr = expr;

            String newAlias = getAlias(fieldOrMethod, opAnnotation, alias);

            //expr = tryAppendAlias(expr, newAlias);

            expr = tryAppendDistinctAndAlias(expr, newAlias, opAnnotation);

            if (isGroupBy) {
                //增加GroupBy字段

                //多数数据库的 group by 语句不支持别名，因为GROUP BY子句比SELECT子句先执行
                //SQL按照如下顺序执行查询：
                //FROM子句
                //WHERE子句
                //GROUP BY子句
                //HAVING子句
                //SELECT子句
                //ORDER BY子句
                groupBy(oldExpr, holder.value);

            }

            tryAppendHaving(opAnnotation, oldExpr, holder, value);

            // ORDER BY 也不能使用别名
            tryAppendOrderBy(oldExpr, newAlias, opAnnotation);

            select(expr, holder.value);

            //@todo 目前由于Hibernate 5.2.17 版本对 Tuple 返回的数据无法获取字段名称，只好通过 druid 解析 SQL 语句
            appendColumnMap(expr, newAlias, fieldOrMethod, name);

        });

    }

    private static String getAlias(Object fieldOrMethod, Annotation opAnnotation, String newAlias) {

        if (!hasText(newAlias)) {
            newAlias = ClassUtils.getValue(opAnnotation, "alias", false);
        }

        if (!hasText(newAlias)
                && fieldOrMethod != null) {

            if (fieldOrMethod instanceof CharSequence) {
                newAlias = (String) fieldOrMethod;
            } else if (fieldOrMethod instanceof Field) {
                newAlias = ((Field) fieldOrMethod).getName();
            } else if (fieldOrMethod instanceof Method) {
                newAlias = ((Method) fieldOrMethod).getName();
                if (newAlias.startsWith("get") && newAlias.length() > 3) {
                    newAlias = Character.toLowerCase(newAlias.charAt(3)) + newAlias.length() > 4 ? newAlias.substring(4) : "";
                }
            }

        }

        return newAlias;
    }

    //    @Override
    protected void tryAppendOrderBy(String expr, String newAlias, Annotation opAnnotation) {

        OrderBy[] orderByList = ClassUtils.getValue(opAnnotation, "orderBy", false);

        appendOrderBy(expr, newAlias, orderByList);

    }

    protected SelectDao<T> appendOrderBy(final String oldExpr, final String newAlias, OrderBy... orderByList) {

        if (orderByList != null) {

            for (int i = 0; i < orderByList.length; i++) {

                OrderBy orderBy = orderByList[i];

                if (orderBy == null) {
                    continue;
                }

                String expr = (orderBy.useAlias() && hasText(newAlias)) ? newAlias : oldExpr;

                expr = hasText(orderBy.value()) ? aroundColumnPrefix(orderBy.domain(), orderBy.value()) : expr;

                if (hasText(expr)) {
                    addOrderBy(expr, orderBy.order(), orderBy.type());
                }
            }
        }

        return this;
    }


    /**
     * 增加字段映射
     *
     * @param expr
     * @param fieldOrMethod
     * @param name
     */
    protected void appendColumnMap(String expr, String alias, Object fieldOrMethod, String name) {

        String key = hasText(alias) ? alias : QLUtils.parseSelectColumn(expr);

        selectColumnsMap.put(key, new Object[]{name, fieldOrMethod});
    }


    @Override
    protected void checkSafeMode(String whereStatement) {

        if (this.isSafeMode()
                && (!isSafeLimit() && !hasText(whereStatement))) {

            //对于查询默认2个条件满足一个即可

            //如果超出安全模式的限制
            throw new DaoSecurityException("dao safe mode no allow no where statement"
                    + "or no limit or limit over " + getDao().getSafeModeMaxLimit());
        }
    }


    @Override
    protected String genFromStatement() {

        if (hasText(fromStatement)) {
            String from = getText(fromStatement, "").trim();
            boolean hasKey = from.toLowerCase().startsWith("from ");
            return (hasKey ? " " + fromStatement : " From " + fromStatement) + " " + getText(joinStatement.toString(), " ");
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
        } else if (!isCountQueryResult && joinStatement.length() > 0 && fetchAttrs.size() < 1) {
            //如果连接有查询
            builder.insert(0, "Select " + getAlias() + " ");
        } else if (!isCountQueryResult && joinStatement.length() > 0 && fetchAttrs.size() > 0) {
            // builder.insert(0, "Select DISTINCT(" + getText(getAlias(), "")+")");
            builder.insert(0, "Select " + getAlias() + " ");
        }

        String genFromStatement = genFromStatement();

        if (!hasText(genFromStatement)) {
            throw new IllegalArgumentException("from statement not set");
        }

        builder.append(" ").append(genFromStatement);


        //如果不是统计语句，则允许集合抓取语句
        if (!isCountQueryResult && fetchAttrs.size() > 0) {
            fetchAttrs.values().forEach(v -> builder.append(" ").append(v).append(" "));
        }

        String whereStatement = genWhereStatement(EntityOption.Action.Read);

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

        builder.append(" ").append(lastStatements);

        if (this.isSafeMode() && !isCountQueryResult && !hasText(whereStatement) && !isSafeLimit()) {
            throw new DaoSecurityException("Safe mode not allow no where statement or limit [" + rowCount + "] too large, safeModeMaxLimit[1 - " + getDao().getSafeModeMaxLimit() + "], SQL[" + builder + "]");
        }

        return replaceVar(builder.toString());
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


        return count("Select Count(" + column + ") " + genQL(true)
                , getDaoContextValues(), whereParamValues, havingParamValues, lastStatementParamValues);
    }

    /**
     * 如果没有记录，或是记录为null值，都表示为0
     *
     * @param ql
     * @param paramValues
     * @return
     */
    private long count(String ql, Object... paramValues) {

        List<Number> list = dao.find(isNative(), null, -1, rowCount, ql, paramValues);

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
                , havingParamValues
                , lastStatementParamValues);
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

        if (resultClass == null && hasSelectColumns()) {
            //如果没有指定结果类，又有选择列
            //  resultClass = (Class<E>) Tuple.class;
        }

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

        if (converter == null) {
            throw new IllegalArgumentException("converter is null");
        }

        List<Object> queryResult = this.find();

        if (queryResult == null) {
            return Collections.emptyList();
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

        if (targetType == null || targetType == Void.class) {
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

        if (targetType == null || targetType == Void.class) {
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
            return Collections.emptyList();
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

            returnList.add(tryConvertData(data, targetType, valueHolder, maxCopyDeep, ignoreProperties));

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

        if (targetType == null || targetType == Void.class) {
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

        if (targetType == null || targetType == Void.class) {
            return findOne();
        }

        autoSetFetch(targetType);

        return tryConvertData(findOne(), targetType, null, maxCopyDeep, ignoreProperties);

    }

    private <E> E tryConvertData(Object data, Class<E> targetType, ValueHolder<List<List<String>>> valueHolder, int maxCopyDeep, String... ignoreProperties) {

        data = tryConvertArray2Map(data, valueHolder);

        if (data == null || targetType.isInstance(data)) {
            return (E) data;
        }

        return (E) copyProperties(data, targetType, maxCopyDeep, ignoreProperties);

    }


    /**
     * 目的是防止 N + 1 查询
     *
     * @param targetType
     */
    public void autoSetFetch(Class targetType) {

        //如果不是 jpa 或是 没有指定实体类
        if (!getDao().isJpa() || targetType == null || targetType == Void.class) {
            return;
        }

        this.resultType = targetType;

        ReflectionUtils.doWithFields(targetType, field -> {

                    Fetch fetch = field.getAnnotation(Fetch.class);

                    //绑定到字段的
                    if (!fetch.isBindToField() || fetch.joinType() == Fetch.JoinType.None) {
                        return;
                    }

                    //如果有条件，并且条件不成立
                    if (hasText(fetch.condition())
                            && !evalTrueExpr(null, null, null, fetch.condition())) {
                        return;
                    }

                    String attrName = hasText(fetch.value()) ? fetch.value() : field.getName();

                    attrFetchList.put(field.getDeclaringClass().getName() + "|" + attrName, true);

                    joinFetch(true, fetch.domain(), fetch.joinType(), attrName);

                }, field -> field.isAnnotationPresent(Fetch.class)
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


    public <E> E copyProperties(Object source, E target, int maxCopyDeep, String... ignoreProperties) {
        try {
            ObjectUtil.fetchPropertiesFilters.set(Arrays.asList((key) -> attrFetchList.getOrDefault(key, false)));
            return (E) ObjectUtil.copyProperties(source, target, maxCopyDeep, ignoreProperties);
        } finally {
            ObjectUtil.fetchPropertiesFilters.set(null);
        }
    }

    @Override
    public <E> E copyProperties(Object source, E target, String... ignoreProperties) {
        return copyProperties(source, target, -1, ignoreProperties);
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
     * @param data        数组对象
     * @param valueHolder 数据缓存
     * @return
     * @todo 优化性能，直接转换成对象
     */
    public Object tryConvertArray2Map(Object data, ValueHolder<List<List<String>>> valueHolder) {

        if (data == null) {
            return data;
        }

        //只有一个元素时，hibernate不会返回数组，直接返回数据
        if (this.selectColumns.size() == 1
                && !data.getClass().isArray()) {
            data = new Object[]{data};
        }

        if (!data.getClass().isArray()) {
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
    public SelectDao<T> count(String expr, String alias, Map<String, Object>... paramValues) {
        return processStat(2, expr, alias, paramValues);
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
    public SelectDao<T> groupByAndSelect(String expr, String alias, Map<String, Object>... paramValues) {

        Annotation groupBy = QueryAnnotationUtil.getAnnotation(GroupBy.class);

        processStatAnno(null, null, new Annotation[]{groupBy}, expr, null, paramValues, groupBy, alias);

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
