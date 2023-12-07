package com.levin.commons.dao.support;


import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.NOT;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.misc.PreDelete;
import com.levin.commons.dao.annotation.misc.PreUpdate;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import com.levin.commons.dao.annotation.misc.Validator;
import com.levin.commons.dao.annotation.order.OrderByList;
import com.levin.commons.dao.annotation.update.Immutable;
import com.levin.commons.dao.exception.DaoSecurityException;
import com.levin.commons.dao.exception.StatementBuildException;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.utils.ClassUtils;
import com.levin.commons.utils.ExceptionUtils;
import com.levin.commons.utils.MapUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.levin.commons.dao.util.QueryAnnotationUtil.*;
import static org.springframework.util.StringUtils.*;


/**
 * 条件构建器实现
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 * @param <CB>
 */
public abstract class ConditionBuilderImpl<T, CB extends ConditionBuilder>
        implements ConditionBuilder<CB> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DELIMITER = " , ";

    protected javax.validation.Validator validator;

    //////////////////////////////////////////////////////////
    protected Class<T> entityClass;

    protected String tableName;

    protected String alias;

    private boolean canChangeNativeQL = true;

    private boolean nativeQL = false;

    ///////////////////////////////////////////////////////////

    final List whereParamValues = new ArrayList(5);

    //加到最后的语句
    final SimpleList<String> lastStatements = new SimpleList<>(true, new ArrayList(2), " ");

    final List lastStatementParamValues = new ArrayList(2);

    @Getter
    @Setter
    int rowStart = -1, rowCount = 512;


    private final ExprNode whereExprRootNode = new ExprNode(AND.class.getSimpleName(), true);

    private Map<String, Object> context;

    protected ParameterNameDiscoverer parameterNameDiscoverer;

    protected final List<TargetOption> targetOptionList = new ArrayList<>(5);

    public static final String BASE_PACKAGE_NAME = Eq.class.getPackage().getName();

    public static final String LOGIC_PACKAGE_NAME = AND.class.getPackage().getName();

    protected boolean safeMode = true;

    // protected String localParamPlaceholder = null;

    protected boolean filterLogicDeletedData = true;

    protected transient MiniDao dao;

    @Getter
    protected Integer safeModeMaxLimit = 2000;

    //默认不过滤空置的
    protected boolean disableEmptyValueFilter = true;

    /**
     * 别名缓存
     */
    protected final ContextHolder<String, Class<?>> aliasMap = ContextHolder.buildContext(true);

    {
        aliasMap.setKeyConverter(key -> key.trim().toLowerCase());
    }

    /**
     * 当原生查询时，是否允许名称转换，默认是允许的
     */
//    @Getter
    protected boolean enableNameConvert = true;

    /**
     * 自动在最后面追加 limit 语句
     * <p>
     * 查询时无效
     */
//    @Getter
    protected boolean autoAppendLimitStatement = false;

    /**
     * 默认自动追加更新或是删除的条件
     */
    private boolean autoAppendOperationCondition = true;

    private static final ThreadLocal<BiFunction<String, Map<String, Object>[], Object>> elEvalFuncThreadLocal = new ThreadLocal<>();

    //已经处理过的类
    private final List walkedObjects = new ArrayList<>(6);

    protected ConditionBuilderImpl(MiniDao miniDao, boolean isNative) {

        dao = miniDao;
        this.nativeQL = (isNative);
        this.entityClass = null;
        this.tableName = null;
        this.alias = null;

    }

    public ConditionBuilderImpl(MiniDao miniDao, boolean isNative, Class<T> entityClass, String alias) {

        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass is null");
        }

        dao = miniDao;
        this.entityClass = entityClass;
        this.tableName = null;
        this.nativeQL = (isNative);
        this.alias = alias;

    }

    public ConditionBuilderImpl(MiniDao miniDao, boolean isNative, String tableName, String alias) {

        if (tableName == null) {
            throw new IllegalArgumentException("tableName is null");
        }
        dao = miniDao;
        this.entityClass = null;
        this.nativeQL = (isNative);
        this.alias = alias;

        setTableName(tableName);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    public ParameterNameDiscoverer getParameterNameDiscoverer() {

        if (parameterNameDiscoverer == null) {
            parameterNameDiscoverer = new MethodParameterNameDiscoverer();
        }

        return parameterNameDiscoverer;
    }

    public CB setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        return (CB) this;
    }

    @Override
    public CB setSafeModeMaxLimit(Integer maxLimit) {
        this.safeModeMaxLimit = maxLimit;
        return (CB) this;
    }

    public javax.validation.Validator getValidator() {
        return validator;
    }


    public CB setValidator(javax.validation.Validator validator) {
        this.validator = validator;
        return (CB) this;
    }

    protected EntityOption getEntityOption() {

        //
        return hasEntityClass() ? entityClass.getAnnotation(EntityOption.class) : null;

        // QueryAnnotationUtil.getEntityOption(entityClass);
    }

    protected void checkAction(EntityOption.Action action, Consumer checkFailCallback) {
        if (isDisable(action)) {
            if (checkFailCallback != null) {
                checkFailCallback.accept(action);
            } else {
                throw new DaoSecurityException(" " + entityClass + " disable " + action + " action");
            }
        }
    }

    protected boolean isDisable(EntityOption.Action... actions) {

        EntityOption entityOption = getEntityOption();

        if (actions == null
                || actions.length < 1
                || entityOption == null
                || entityOption.disableActions() == null
                || entityOption.disableActions().length < 1) {

            //没有禁止就是允许
            return false;
        }

        for (EntityOption.Action allowableAction : entityOption.disableActions()) {
            for (EntityOption.Action action : actions) {
                if (action != null && action.equals(allowableAction)) {
                    return true;
                }
            }
        }

        return false;
    }


    protected boolean hasLogicDeleteField(EntityOption entityOption) {

        return entityOption != null
                && StringUtils.hasText(entityOption.logicalDeleteFieldName())
                && StringUtils.hasText(entityOption.logicalDeleteValue());
    }

//    protected boolean hasLogicDeleteStatement(EntityOption entityOption) {
//        return entityOption != null
//                && StringUtils.hasText(entityOption.logicalDeleteSetValueStatement())
//                && StringUtils.hasText(entityOption.logicalDeleteDeterminedStatement());
//    }


    /**
     * 禁止安全模式
     * 在安全模式下，不允许无条件的更新或是删除
     */
    @Override
    public CB disableSafeMode() {
        safeMode = false;
        return (CB) this;
    }


    /**
     * 安全模式
     *
     * @return
     */
    @Override
    public boolean isSafeMode() {
        return safeMode;
    }


    @Override
    public CB setContext(Map<String, Object> context) {
        this.context = context;
        return (CB) this;
    }

    /**
     * 获取上下文
     *
     * @return
     */
    @Override
    public Map<String, Object> getContext() {
        return getLocalContext(true);
    }

    /**
     * @return
     */
    protected Map<String, Object> getLocalContext(boolean autoInit) {

        if (autoInit && this.context == null) {
            this.context = new ConcurrentReferenceHashMap<>();
        }

        return this.context;
    }


    @Override
    public boolean isSafeLimit() {
        return rowCount > 0 && rowCount <= getDao().getSafeModeMaxLimit();
    }

    @Override
    public CB disableNameConvert() {

        enableNameConvert = false;

        return (CB) this;
    }

    @Override
    public CB and() {
        return and(true);
    }

    @Override
    public CB and(Boolean valid) {
        beginLogic(AND.class.getSimpleName(), Boolean.TRUE.equals(valid));
        return (CB) this;
    }

    @Override
    public CB or() {
        return or(true);
    }

    @Override
    public CB or(Boolean valid) {
        beginLogic(OR.class.getSimpleName(), Boolean.TRUE.equals(valid));
        return (CB) this;
    }

    @Override
    public CB not() {
        return not(true);
    }

    @Override
    public CB not(Boolean valid) {
        beginLogic(NOT.class.getSimpleName(), Boolean.TRUE.equals(valid));
        return (CB) this;
    }

    @Override
    public CB end() {
        endLogic(true);
        return (CB) this;
    }

    @Override
    public CB limit(int rowStartPosition, int rowCount) {
        this.rowStart = rowStartPosition;
        this.rowCount = rowCount;
        return (CB) this;
    }

    @Override
    public CB enableAutoAppendLimitStatement(boolean enable) {

        autoAppendLimitStatement = enable;

        return (CB) this;
    }

    @Override
    public CB disableOperationCondition() {

        autoAppendOperationCondition = false;

        return (CB) this;
    }

    /**
     * 获取 limit 语句
     *
     * @return
     */
    protected String getLimitStatement() {

        String ql = "";

        if (autoAppendLimitStatement || isNative()) {

            //hibernate jpql 不支持 limit 语句。
            if (rowStart > 0) {
                ql = " " + rowStart;
            }

            if (rowCount > 0) {
                ql = (ql.length() > 0 ? " , " : " ") + rowCount;
            }

            if (ql.length() > 0) {
                ql = " limit " + ql;
            }
        }

        return ql;
    }

    /**
     * 设置查询的分页
     *
     * @param pageIndex 第几页，从1开始
     * @param pageSize  分页大小
     * @return
     */
    @Override
    public CB page(int pageIndex, int pageSize) {


        if (pageIndex < 1) {
            pageIndex = 1;
        }

        this.rowStart = (pageIndex - 1) * pageSize;


        this.rowCount = pageSize;

        return (CB) this;
    }

    @Override
    public CB page(Paging paging) {

        if (paging != null) {
            page(paging.getPageIndex(), paging.getPageSize());
        }

        return (CB) this;
    }

    @Override
    public CB filterLogicDeletedData(boolean enable) {

        filterLogicDeletedData = enable;

        return (CB) this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public CB where(Boolean isAppend, String conditionExpr, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)) {
            appendToWhere(conditionExpr, paramValues, false);
        }

        return (CB) this;
    }


    @Override
    public CB appendToLast(Boolean isAppend, String expr, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend)
                && hasText(expr)) {
            lastStatements.add(expr);
            lastStatementParamValues.add(paramValues);
        }

        return (CB) this;
    }

    protected List getLastStatementParamValues() {
        return lastStatements.isEmpty() ? Collections.emptyList() : lastStatementParamValues;
    }

    @Override
    public CB appendByQueryObj(Object... queryObjs) {

        walkObject(queryObjs);

        return (CB) this;
    }


    @Override
    public CB appendByMethodParams(Object methodOwnerBean, Method method, Object... args) {

        walkMethod(methodOwnerBean, method, args);

        return (CB) this;
    }

    /**
     * 按文本表达式构建查询条件
     * <p/>
     * 如：属性名Q_Not_Like_name  值 llw，表f示会生成查询条件 name not like '%llw%'
     * 注意时间的文本表达式："2016/07/16 23:59:07"
     * <p/>
     * <p/>
     * param.put("Q_name", "llw");
     * param.put("nickName", "llw");
     * param.put("Q_Like_name", "llw");
     * param.put("Q_Gt_date1", new Date());
     * param.put("Q_Lt_date2", new Date());
     * param.put("Q_Gte_date3", new Date());
     * param.put("Q_Lte_date4", new Date());
     * param.put("Q_Not_gt_date5", new Date());
     * param.put("Q_NotLike_date6", new Date());
     * param.put("Q_NotEq_date7", new Date());
     * <p/>
     * param.put("Q_NotNull_date8","2016/07/16 23:59:07");
     * param.put("Q_NotLike_date9", new Date());
     * <p/>
     * <p/>
     * param.put("Q_NotLike_", "llw");
     * param.put("Q_name1", "llw");
     * param.put("Q_Not_Contains_name2", "llw");
     * param.put("Q_StartsWith_name3", "llw");
     * <p/>
     * param.put("Q_Not_EndsWith_name5", "llw");
     * param.put("name6", "llw");
     *
     * @param paramPrefix ，如果Q_
     * @param queryParams
     * @return
     */
    @Override
    public CB appendByEL(String paramPrefix, Map<String, Object>... queryParams) {

        walkMap(paramPrefix, queryParams);

        return (CB) this;
    }


    @Override
    public CB appendByAnnotations(Boolean isAppend, @javax.validation.constraints.NotNull String attrName, Object attrValue, Class<? extends Annotation>... annoTypes) {

        if (Boolean.TRUE.equals(isAppend)) {
            processAttr(null, null, attrName, QueryAnnotationUtil.getAnnotations(annoTypes), null, attrValue);
        }

        return (CB) this;
    }


    private CB processAnno(int callMethodDeep, String expr, Object value) {

//        if (!StringUtils.hasText(expr)) {
//            throw new IllegalArgumentException("expr has no content");
//        }

        final Exception exception = new UnsupportedOperationException(expr);

        String name = exception.getStackTrace()[callMethodDeep].getMethodName();

        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        Annotation annotation = QueryAnnotationUtil.getAllAnnotations().get(name);

        if (annotation == null) {
            throw new IllegalArgumentException("Annotation " + name + " not found");
        }

        Op op = getOp(annotation);

        //如果没有操作，或是操作不需要参数，或是禁止空值过滤，或是参数非空，都加入条件表达式
        if (op == null
                || !op.isNeedParamExpr()
                || disableEmptyValueFilter
                || !isNullOrEmptyTxt(value)) {

            processWhereCondition(null, null, expr, value, null, annotation);

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("注解 " + name + " 对应的参数值为空，忽略条件，调用堆栈：" + ExceptionUtils.getAllCauseInfo(exception, " -> "));
            }
        }

        return (CB) this;
    }

    @Override
    public CB disableEmptyValueFilter() {

        this.disableEmptyValueFilter = true;

        return (CB) this;
    }


    @Override
    public CB enableEmptyValueFilter() {

        this.disableEmptyValueFilter = false;

        return (CB) this;
    }

    /**
     * is null
     *
     * @param entityAttrName 如 name
     * @return
     */
    @Override
    public CB isNull(String entityAttrName) {
        return processAnno(2, entityAttrName, null);
    }

    /**
     * is not null
     *
     * @param entityAttrName 如 name
     * @return
     */
    @Override
    public CB isNotNull(String entityAttrName) {
        return processAnno(2, entityAttrName, null);
    }


    @Override
    public CB isNullOrEq(String entityAttrName, Object paramValue) {
        appendByAnnotations(true, entityAttrName, paramValue, OR.class, IsNull.class, Eq.class, END.class);
        return (CB) this;
    }

    /**
     * =
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB eq(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB notEq(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * >
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB gt(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * <
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB lt(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * >=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB gte(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * <=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public CB lte(String entityAttrName, Object paramValue) {
        return processAnno(2, entityAttrName, paramValue);
    }

    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public CB between(String entityAttrName, Object... paramValues) {
        return processAnno(2, entityAttrName, paramValues);
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public CB in(String entityAttrName, Object... paramValues) {
        return processAnno(2, entityAttrName, paramValues);
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public CB notIn(String entityAttrName, Object... paramValues) {
        return processAnno(2, entityAttrName, paramValues);
    }


    /**
     * exist (sub query)
     *
     * @param exprOrQueryObj
     * @return
     */
    @Override
    public CB exists(Object exprOrQueryObj, Object... paramValues) {
        return processOp(Op.Exists.getOperator(), exprOrQueryObj, paramValues);
    }

    /**
     * not exist (sub query)
     * <p>
     * * @param exprOrQueryObj
     *
     * @return
     */
    @Override
    public CB notExists(Object exprOrQueryObj, Object... paramValues) {
        return processOp(Op.NotExists.getOperator(), exprOrQueryObj, paramValues);
    }

    protected CB processOp(String op, Object exprOrQueryObj, Object paramValues) {

        String expr = "";

        paramValues = ExprUtils.tryGetFirstElementIfOnlyOne(paramValues);

        if (exprOrQueryObj instanceof CharSequence) {

            expr = exprOrQueryObj.toString();

        } else if (exprOrQueryObj instanceof StatementBuilder) {

            StatementBuilder builder = (StatementBuilder) exprOrQueryObj;

            expr = builder.genFinalStatement();

            //原有参数放回去
            paramValues = Arrays.asList(paramValues, builder.genFinalParamList());

        } else {
            //忽略 paramValues
            return processAnno(3, "", exprOrQueryObj);
        }

        if (hasText(expr)) {
            expr = op + "(" + expr + ")";
        }

        appendToWhere(expr, paramValues, false);

        return (CB) this;

    }


    /**
     * like %keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public CB contains(String entityAttrName, String keyword) {
        return processAnno(2, entityAttrName, keyword);
    }

    /**
     * like keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public CB startsWith(String entityAttrName, String keyword) {
        return processAnno(2, entityAttrName, keyword);
    }

    /**
     * like %keyword
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public CB endsWith(String entityAttrName, String keyword) {
        return processAnno(2, entityAttrName, keyword);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 是否已经设置了要查询的实体或是表名
     *
     * @return
     */
    protected boolean hasValidQueryEntity() {
        return hasEntityClass() || hasText(tableName);
    }

    protected boolean hasEntityClass() {
        return (entityClass != null && entityClass != Void.class);
    }


    protected CB setTableName(String tableName) {

        this.tableName = tableName;

        if (!hasEntityClass()) {

            Matcher matcher = ExprUtils.entityVarStylePattern.matcher(this.tableName);

            if (matcher.matches()) {
                this.tableName = matcher.group(2);
            }

            this.entityClass = (Class<T>) dao.getEntityClass(this.tableName.trim());

            if (entityClass != null) {
                //如果表名是类名，做个自动转换
                this.tableName = dao.getTableName(entityClass);
            }
        }

        return (CB) this;
    }

    /**
     * 通过名称转换器转换表名
     *
     * @param tableName
     * @return
     */
    protected String convertTableNameByNamingStrategy(String tableName) {

        if (isNative()
                && enableNameConvert
                && hasText(tableName)
                && !containsWhitespace(tableName)
                && getDao() != null) {

            //转换名称
            tableName = getDao().getNamingStrategy().toPhysicalTableName(tableName, null);

        }

        return tableName;
    }

    /**
     * 转换列名
     *
     * @param columnName
     * @return
     */
    protected String convertColumnNameByNamingStrategy(String columnName) {

        if (isNative()
                && enableNameConvert
                && hasText(columnName)
                && !containsWhitespace(columnName)
                && getDao() != null) {

            columnName = getDao().getNamingStrategy().toPhysicalColumnName(columnName, null);

        }

        return columnName;
    }

    /**
     * 自动更新表名
     *
     * @return
     */
    protected CB tryUpdateTableName() {

        if (isNative()
                && hasEntityClass()
                && !StringUtils.hasText(tableName)) {
            this.tableName = getDao().getTableName(entityClass);
        }

        return (CB) this;
    }

    /**
     * 生成语句
     *
     * @return
     */
    protected String genEntityStatement() {

        if (isNative()) {

            tryUpdateTableName();

            Matcher matcher = ExprUtils.entityVarStylePattern.matcher(tableName);

            if (!matcher.matches()) {
                this.tableName = convertTableNameByNamingStrategy(tableName);
            }

            if (hasText(this.tableName)) {
                return tableName + " " + getText(alias, " ");
            }

        }

        if (!isNative() && hasEntityClass()) {
            return entityClass.getName() + " " + getText(alias, " ");
        }

        throw new StatementBuildException("entityClass or tableName is no valid");
    }

    /**
     * 转换名称
     *
     * @param column
     * @return
     */
    protected String aroundColumnPrefix(String column) {
        return aroundColumnPrefix(null, column);
    }


    /**
     * 重点方法
     *
     * @param domain
     * @param column
     * @return
     */
    protected String aroundColumnPrefix(String domain, String column) {

        if (!hasText(column)) {
            return "";
        }

        //去除空格
        column = column.trim();

        boolean hasDomain = hasText(domain);

        //关键逻辑点
        //判定一个字段表达式 是否是一个函数，或是表达式
        //@todo 逻辑风险点
        // 1、包含占位符
        // 2、包含空格（说明是个表达式）
        // 3、首字符不是字母或下线
        //@Fix bug 20200227
        if (column.contains(getParamPlaceholder().trim()) //如果包含参数占位符
                || StringUtils.containsWhitespace(column) //包含白空格
                || !(Character.isLetter(column.charAt(0)) || '_' == column.charAt(0)) //如果首字符不是字母
                || !column.matches("[\\w._]+") //如果是特殊字符
            //  || (!hasDomain && column.contains("."))
        ) {
            //直接返回
            return column;
        }

        //如果没有指定别名，但有包含点，
        if (!hasDomain && column.contains(".")) {
            return tryGetPhysicalColumnName(column);
        }

        //如果没有指定别名，则用默认别名
        if (!hasDomain) {
            domain = alias;
        }

        // :?P

        //如果别名指定为 null，按特殊值处理
        if (C.BLANK_VALUE.equalsIgnoreCase(domain)) {
            domain = "";
        }

        String prefix = getText(domain, "", ".", "");

        return tryGetPhysicalColumnName(column.trim().startsWith(prefix) ? column : prefix + column);
    }

    protected String tryGetPhysicalColumnName(String column) {

        if (isNative()) {

            //获取别名
            int indexOf = column.indexOf(".");

            if (indexOf > 0) {

                //转换成小写
                String domain = column.substring(0, indexOf).trim().toLowerCase();

                column = column.substring(indexOf + 1).trim();

                //优先使用注解中的列名
                column = getDao().getColumnName(domain.equalsIgnoreCase(alias) ? entityClass : aliasMap.get(domain), column);

                column = domain + "." + column;

            } else {

                column = getDao().getColumnName(entityClass, column);
            }

        }

        return column;
    }

    protected String genFromStatement() {
        return " From " + genEntityStatement();
    }


    /**
     * 替换文本变量，替换字段
     *
     * @param ql 查询语句
     * @return
     */
    protected String replaceVar(String ql) {
        return ExprUtils.replace(ql, getDaoContextValues(), true, this::aroundColumnPrefix, this::tryToPhysicalTableName);
    }


    protected String tryToPhysicalTableName(String className) {

        if (isNative()) {
            return getDao().getTableName(className);
        }

        return className;
    }


    protected CB setTargetOption(Object hostObj, TargetOption targetOption) {

        if (targetOption == null) {
            return (CB) this;
        }

        //重复的不再处理
        if (targetOptionList.contains(targetOption)) {
            return (CB) this;
        }

        targetOptionList.add(targetOption);

        //
        if (hasValidQueryEntity()) {
            return (CB) this;
        }

        this.entityClass = (Class<T>) targetOption.entityClass();

        setTableName(targetOption.tableName());

        this.setNative(targetOption.nativeQL());

        this.alias = targetOption.alias();

        //如果是第一个
        this.safeMode = targetOption.safeMode();

//        if (hasText(targetOption.fromStatement())) {
//            setFromStatement(targetOption.fromStatement());
//        }

        //  this.where(targetOption.fixedCondition());

        if (targetOption.maxResults() > 0) {
            this.rowCount = targetOption.maxResults();
        }

        tryUpdateTableName();

        //使用 join 语句的方式增加连接语句
//        String joinStatement = ExprUtils.genJoinStatement(getDao(), isNative()
//                , aliasMap::put
//                , this::tryToPhysicalTableName, this::tryToPhysicalColumnName
//                , entityClass, tableName, alias, targetOption.joinOptions());
//
//        if (hasText(joinStatement)) {
//            join(true, joinStatement);
//        }

        join(true, targetOption.joinOptions());
        join(true, targetOption.simpleJoinOptions());

        return (CB) this;
    }


    /**
     * 只对SelectDao 有效
     * 其它 Dao 默认忽略这个部分
     *
     * @param isAppend
     * @param joinOptions
     * @return
     */
    protected CB join(Boolean isAppend, JoinOption... joinOptions) {

        //Nothing to do

        return (CB) this;
    }

    protected CB join(Boolean isAppend, SimpleJoinOption... joinOptions) {

        //Nothing to do

        return (CB) this;
    }

    protected CB setQueryOption(Object... queryObjs) {

        if (queryObjs == null
                || queryObjs.length == 0
                || hasValidQueryEntity()) {
            return (CB) this;
        }

        Arrays.stream(queryObjs)
                .filter(o -> o instanceof QueryOption)
                .map(o -> (QueryOption) o).forEachOrdered(queryOption -> {

                    if (hasValidQueryEntity()) {
                        return;
                    }

                    this.entityClass = (Class<T>) queryOption.getEntityClass();

                    this.setNative(queryOption.isNative());

                    // tableName = queryOption.getEntityName();

                    setTableName(queryOption.getEntityName());

                    alias = queryOption.getAlias();

                    tryUpdateTableName();

                    if (hasValidQueryEntity()) {

//                    String joinStatement = ExprUtils.genJoinStatement(getDao(), isNative()
//                            , aliasMap::put
//                            , this::tryToPhysicalTableName, this::tryToPhysicalColumnName
//                            , entityClass, tableName, alias, queryOption.getJoinOptions());
//                    if (hasText(joinStatement)) {
//                        join(true, joinStatement);
//                    }

                        join(true, queryOption.getJoinOptions());

                        join(true, queryOption.getSimpleJoinOptions());
                    }

                });


        return (CB) this;
    }


    protected CB setPaging(Object queryObj) {

        //如果不是分页对象
        if ((queryObj instanceof Paging)) {
            page(Paging.class.cast(queryObj));
        }

        return (CB) this;
    }

    protected void setFromStatement(String fromStatement) {
        //  throw new UnsupportedOperationException(getClass().getName() + " setFromStatement");
    }

    protected CB join(Boolean isAppend, String... joinStatements) {
//        throw new StatementBuildException("Only SelectDao support this operation");
        return (CB) this;
    }

//////////////////////////////////////////////

    protected void beforeWalkMethod(Object methodOwnerBean, Method method, Object[] args) {

        //优先
        setQueryOption(args);

        //参数
        int i = 0;

        for (Annotation[] annotations : method.getParameterAnnotations()) {

            for (Annotation annotation : annotations) {
                if (annotation instanceof TargetOption) {
                    setTargetOption(args[i], (TargetOption) annotation);
                }
            }

            i++;
        }

        //先设置方法上的注解
        setTargetOption(null, method.getAnnotation(TargetOption.class));
        setTargetOption(null, method.getDeclaringClass().getAnnotation(TargetOption.class));

    }


    protected void afterWalkMethod(Object methodOwnerBean, Method method, Object[] args) {

    }

    protected void walkMethod(Object methodOwnerBean, Method method, Object[] args) {

        //如果是忽略的方法
        if (method.getAnnotation(Ignore.class) != null) {
            return;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        String[] parameterNames = getParameterNameDiscoverer().getParameterNames(method);

        if (parameterNames == null || parameterTypes == null
                || parameterNames.length != parameterTypes.length) {
            throw new IllegalStateException("method [" + method + "] can't get param name");
        }

        for (String parameterName : parameterNames) {
            if (parameterName == null || !hasText(parameterName)) {
                throw new IllegalStateException("method [" + method + "] can't get param name");
            }
        }

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        beforeWalkMethod(methodOwnerBean, method, args);

        for (int i = 0; i < parameterTypes.length; i++) {

            String pName = parameterNames[i];

            Annotation[] varAnnotations = parameterAnnotations[i];

            //参数获取定义的数据类型
            Class<?> pType = ResolvableType.forMethodParameter(method, i, methodOwnerBean != null ? methodOwnerBean.getClass() : null)
                    .resolve(parameterTypes[i]);

            Object value = args[i];

            processAttr(null, null, pName, varAnnotations, pType, value);

        }

        //之前之后
        afterWalkMethod(methodOwnerBean, method, args);

    }

    /**
     * 解析对象所有的属性，过滤并调用回调
     *
     * @param queryObjs
     */
    public void walkObject(Object... queryObjs) {

        if (queryObjs == null || queryObjs.length == 0) {
            return;
        }

        //转换为List，排查已经加载过的类
        List<Object> queryObjList = filterQueryObjSimpleType(expandAndFilterNull(null, Arrays.asList(queryObjs)), walkedObjects::contains);

        if (queryObjList.isEmpty()) {
            return;
        }

        //全部加入已经处理过的对象，防止对象被反复处理
        walkedObjects.addAll(queryObjList);

        if (!hasValidQueryEntity()) {
            //如果没有有效的查询实体
            EntityClassSupplier supplier = (EntityClassSupplier) queryObjList.stream()
                    .filter(o -> o instanceof EntityClassSupplier)
//                    .filter(o -> isJpaEntityClass(o.getClass()))
                    .filter(o -> getDao().isEntityClass(o.getClass()))

                    .findFirst()
                    .orElse(null);

            if (supplier != null) {
                this.entityClass = (Class<T>) supplier.get();
                this.alias = supplier.getAlias();
            }
        }

        //1、设置并清除参数中的实体类，第2优先级
        queryObjList = tryGetEntityClassAndClear(
                //展开嵌套参数，过滤简单的类型，
                queryObjList,
                //试图设置
                hasValidQueryEntity() ? null : c -> {
                    this.entityClass = (Class<T>) c;
                    this.alias = EntityClassSupplier.getAlias(this.entityClass);
                }
        );

        if (queryObjList.isEmpty()) {
            return;
        }

        //覆盖原有的参数
        queryObjs = queryObjList.toArray();
        // 2、QueryOption 第3优先级 设置查询目标
        setQueryOption(queryObjs);

        //3、设置参数实体类，第4优先级
        queryObjList.stream().filter(Objects::nonNull)
                .map(o -> (o instanceof Class) ? (Class<?>) o : o.getClass())
                .filter(c -> c.isAnnotationPresent(TargetOption.class))
                .forEachOrdered(c -> setTargetOption(null, c.getAnnotation(TargetOption.class)));

        for (Object queryValueObj : queryObjList) {

            if (queryValueObj == null) {
                continue;
            }

            //如果是增强器，则执行增强功能
            if (queryValueObj instanceof Consumer) {
                ((Consumer) queryValueObj).accept(this);
                continue;
            }

            //
            final boolean isClassCurrQueryObj = queryValueObj instanceof Class;

            Class<?> typeClass = isClassCurrQueryObj ? (Class<?>) queryValueObj : queryValueObj.getClass();

            if (!isClassCurrQueryObj) {

                //对注解的支持 PostConstruct
                ClassUtils.invokePostConstructMethod(queryValueObj);

                if (this instanceof UpdateDao) {
                    ClassUtils.invokeMethodByAnnotationTag(queryValueObj, false, PreUpdate.class);
                } else if (this instanceof DeleteDao) {
                    ClassUtils.invokeMethodByAnnotationTag(queryValueObj, false, PreDelete.class);
                }

                //没有回调时，表示本地调用
                //尝试设置分页
                setPaging(queryValueObj);

                //尝试设置查询目标实体
                //
                //  setTargetOption(queryValueObj, typeClass.getAnnotation(TargetOption.class));

                //特别处理
                if (queryValueObj instanceof Map) {
                    walkMap("", (Map) queryValueObj);
                    continue;
                }
            }

            //关键方法，必须保证返回的顺序是，父类字段优先出现，然后才是子类的字段
            List<Field> fields = QueryAnnotationUtil.getNonStaticFields(typeClass);

            ResolvableType rootType = ResolvableType.forType(typeClass);

            if (!isClassCurrQueryObj) {
                processCtxVar(queryValueObj, fields);
            }

            List<?> contexts = isClassCurrQueryObj ? null : DaoContext.getDaoContexts(queryValueObj);

            final String pkgStartsWith = BASE_PACKAGE_NAME + ".";

            //查找类上的注解
            Annotation[] annotationsOnClass = null;

            if (isClassCurrQueryObj) {
                List<Annotation> anList = Stream.of(typeClass.getAnnotations()).filter(a -> a.annotationType().getName().startsWith(pkgStartsWith)).collect(Collectors.toList());
                annotationsOnClass = anList.toArray(new Annotation[anList.size()]);
            }

            //开始处理字段
            for (Field field : fields) {

                //忽略字段
                if (QueryAnnotationUtil.isIgnore(field)) {
                    continue;
                }

                ResolvableType fieldRT = ResolvableType.forField(field, rootType);

                Class<?> targetType = fieldRT.resolve(field.getType());

                String name = field.getName();

                try {

                    Object value = null;

                    if (!isClassCurrQueryObj) {

                        field.setAccessible(true);

                        //处理注入值
                        com.levin.commons.service.support.ValueHolder<Object> valueHolder =
                                field.isAnnotationPresent(InjectVar.class) ?
                                        DaoContext.getVariableInjector().getOutputValueByBean(queryValueObj, field, contexts) : null;

                        if (valueHolder != null) {

                            if (StringUtils.hasText(valueHolder.getName())) {
                                name = valueHolder.getName();
                            }

                            if (valueHolder.hasValue()) {
                                value = valueHolder.getValue();
                            }

                            if ((valueHolder.getType() instanceof Class)) {
                                targetType = (Class<?>) valueHolder.getType();
                            } else if (value != null) {
                                targetType = value.getClass();
                            }

                        } else {
                            value = field.get(queryValueObj);
                        }
                    }

                    Annotation[] varAnnotations = field.getAnnotations();

                    if (isClassCurrQueryObj
                            && Stream.of(varAnnotations).noneMatch(a -> a.annotationType().getName().startsWith(pkgStartsWith))) {
                        //使用类上的注解
                        varAnnotations = annotationsOnClass;
                    }

                    processAttr(isClassCurrQueryObj ? null : queryValueObj
                            , field, name, varAnnotations
                            , targetType, value);

                } catch (Exception e) {
                    throw new StatementBuildException("处理注解失败，字段:" + field + ", " + e.getMessage(), e);
                }

            }

            if (!isClassCurrQueryObj) {
                //拷贝对象的字段，可能会被作为命名的查询参数
                whereParamValues.add(QueryAnnotationUtil.copyMap(true, null, ObjectUtil.copyField2Map(queryValueObj, null)));
            }

        }
    }

    /**
     * 出来上下文变量
     *
     * @param queryValueObj
     * @param fields
     */
    private void processCtxVar(Object queryValueObj, List<Field> fields) {

        //开始处理字段
        for (Field field : fields) {

            field.setAccessible(true);

            processCtxVar(queryValueObj, field, field.getAnnotation(CtxVar.class));

            CtxVar.List list = field.getAnnotation(CtxVar.List.class);

            if (list != null) {
                for (CtxVar ctxVar : list.value()) {
                    processCtxVar(queryValueObj, field, ctxVar);
                }
            }
        }

        //
    }

    private void processCtxVar(Object queryValueObj, Field field, CtxVar ctxVar) {

        if (ctxVar == null || field == null) {
            return;
        }

        Object exportValue = null;

        try {
            exportValue = field.get(queryValueObj);
        } catch (IllegalAccessException e) {
            throw new StatementBuildException(field + "条件过滤失败", e);
        }

        if (!evalTrueExpr(queryValueObj, exportValue, field.getName(), ctxVar.condition())) {
            return;
        }

        String exportVarName = ctxVar.varName();

        if (!StringUtils.hasText(exportVarName)) {
            exportVarName = field.getName();
        }

        Map<String, Object> localContext = getLocalContext(true);

        //如果不是强制覆盖 并且有变量
        if (!ctxVar.forceOverride() && localContext.containsKey(exportVarName)) {
            return;
        }

        String expr = ctxVar.value();

        if (StringUtils.hasText(expr)) {
            exportValue = ExprUtils.evalSpEL(queryValueObj, expr, buildContextValues(queryValueObj, exportValue, field.getName()));
        }

        localContext.put(exportVarName, exportValue);

    }

    /**
     * 参数的属性名称中带有查询注释说明
     * <p/>
     * 如：属性名Q_Not_Like_name  值 llw，表f示会生成查询条件 name not like '%llw%'
     *
     * @param paramPrefix
     * @param queryParams
     * @return
     */
    public void walkMap(String paramPrefix, Map<String, Object>... queryParams) {

        if (queryParams == null) {
            return;
        }

        boolean hasPrefix = hasText(paramPrefix);

        Map<String, Annotation> annotationMap = QueryAnnotationUtil.getAllAnnotations();

        final String notPrefix = Op.Not.name() + "_";

        for (Map<String, Object> queryParam : queryParams) {

            for (Map.Entry<String, Object> entry : queryParam.entrySet()) {

                String name = entry.getKey();

                // final String oldExpr = name;

                Object paramValue = entry.getValue();

                if (hasPrefix) {
                    //如果不是有效的属性，则忽略
                    if (!name.startsWith(paramPrefix)) {
                        continue;
                    }
                    //去除前缀
                    name = name.substring(paramPrefix.length());
                }

                Op notOp = null;

                //是否包括非的操作
                if (name.startsWith(notPrefix)) {
                    notOp = Op.Not;
                    //去除Not操作前缀
                    name = name.substring((notPrefix).length());
                }

                //默认是等于的操作
                Annotation opAnno = null;

                int idx = name.indexOf("_");

                if (idx != -1) {
                    opAnno = annotationMap.get(name.substring(0, idx));
                }

                if (opAnno != null) {
                    //去除比较操作前缀
                    if (name.length() > idx + 1) {
                        name = name.substring(idx + 1);
                    } else {
                        //logger.trace("");
                        continue;
                    }
                } else {
                    //默认是等于的操作
                    opAnno = annotationMap.get(Eq.class.getSimpleName());
                }

                //如果属性名为null
                if (!hasText(name)) {
                    continue;
                }

                //如果是忽略的条件
                if (opAnno instanceof Ignore) {
                    continue;
                }

                if (notOp != null && opAnno != null) {
                    //@todo 对 Not 没有处理
                    //@Fix
                    // varAnnotations[1] = notOp;

                    Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(opAnno);

                    attributes.put(E_C.not, true);

                    opAnno = AnnotationUtils.synthesizeAnnotation(attributes, opAnno.annotationType(), null);

                    // throw new UnsupportedOperationException(oldExpr);
                }

                Annotation[] varAnnotations = {opAnno};

                processAttr(queryParam, null, name, varAnnotations,
                        paramValue != null ? paramValue.getClass() : null, paramValue);

            }
        }
    }


    /**
     * 核心方法
     * <p>
     * 第一层处理字段或是属性 关联的 组件集合
     * <p>
     * 本方法主要处理逻辑注解
     * <p>
     * 处理单个属性或是方法参数的注解
     *
     * <p>
     * <p>
     * 单个属性，多个注解的处理入口
     * <p>
     * 对象，方法，Map 的 walk 都是这个入口
     * <p>
     * 包含逻辑处理
     *
     * @param bean
     * @param fieldOrMethod
     * @param name
     * @param varAnnotations
     * @param attrType
     * @param value
     */
    public void processAttr(final Object bean, Object fieldOrMethod, String name, Annotation[] varAnnotations, Class<?> attrType, Object value) {

        Assert.hasText(name, "name is empty");

        try {
            //设置脚本执行功能函数
            elEvalFuncThreadLocal.set((expr, ctxs) -> evalExpr(bean, value, name, expr, null, ctxs));

            //如果是包括忽略注解，则直接忽略
            if (isIgnore(varAnnotations)) {
                return;
            }

            //校验数据
            verifyGroupValidation(bean, name, value, findFirstMatched(varAnnotations, Validator.class));

            //支持多个注解
            List<Annotation> logicAnnotations = QueryAnnotationUtil.getLogicAnnotation(name, varAnnotations);

            logicAnnotations.forEach(logicAnnotation -> beginLogicGroup(bean, logicAnnotation, name, value));
            //可以多次逻辑组
            try {
                //如果是忽略的类型
                if (attrType != null && attrType.isAnnotationPresent(Ignore.class)) {
                    return;
                }

                //当前节点是否有效
                if (!whereExprRootNode.currentNode().isValid()) {
                    return;
                }

                processAttr(bean, fieldOrMethod, varAnnotations, name, attrType, value);

            } finally {

                //部分自动关闭
                logicAnnotations.stream()
                        .filter(this::isLogicGroupAutoClose)
                        .forEachOrdered(logicAnnotation -> end());

                endLogicGroup(bean, findFirstMatched(varAnnotations, END.class), value);
            }
            //结束逻辑分组

        } finally {
            elEvalFuncThreadLocal.set(null);
        }
    }


    /**
     * 从当前线程变量中执行脚本
     * 表达式扩展或是替换
     *
     * @param expr
     * @return
     * @see #evalExpr(Object, Object, String, String, List, Map[])
     */
    public static String evalTextByThreadLocal(String expr, Map<String, Object>... exMaps) {

        if (!hasText(expr)
                || !trimWhitespace(expr).startsWith(ExpressionType.SPEL_PREFIX)) {
            return expr;
        }

        BiFunction<String, Map<String, Object>[], Object> func = elEvalFuncThreadLocal.get();

        if (func == null) {
            return expr;
        }

        expr = trimWhitespace(expr).substring(ExpressionType.SPEL_PREFIX.length());

        return trimWhitespace((String) func.apply(expr, exMaps));

    }

    boolean isLogicGroupAutoClose(Annotation logicAnnotation) {
        return Optional.ofNullable(logicAnnotation)
                .map(a -> (boolean) ClassUtils.getValue(logicAnnotation, "autoClose", true))
                .orElse(false);
    }

    /**
     * 重要方法
     * <p/>
     * 过滤出需要处理的注解
     * <p/>
     * 被过滤的注解包括 Ignore、Having、Not 和 所有的逻辑注解
     *
     * @param fieldOrMethod
     * @param varAnnotations
     * @return
     */
    private static List<Annotation> findNeedProcessDaoAnnotations(Object fieldOrMethod, Annotation[] varAnnotations) {

        //@todo 缓存字段

        List<Annotation> result = new ArrayList<>(5);

        if (varAnnotations != null) {
            for (Annotation annotation : varAnnotations) {

                if (annotation == null
                        || annotation instanceof PrimitiveValue
                        || annotation instanceof Validator
                        || annotation instanceof Immutable
                        || annotation instanceof Ignore) {
                    continue;
                }


                String clsName = annotation.annotationType().getName();

                //如果注解的类是在这"com.levin.commons.dao.annotation" 包下，并且不是逻辑操作注解
                //特别关键的判断条件

                if (clsName.startsWith(BASE_PACKAGE_NAME)
                        && !clsName.startsWith(LOGIC_PACKAGE_NAME)) {
                    result.add(annotation);
                }
            }
        }

        return result;
    }


    /**
     * 自动拆解注解集合，并提交消费
     *
     * @param annotation
     * @param consumer
     * @return
     */
    protected boolean autoConsumerIfList(Annotation annotation, boolean isDoConsume, Consumer<Annotation> consumer) {

        Class<? extends Annotation> annotationType = annotation.annotationType();

        if (!"List".contentEquals(annotationType.getSimpleName())
                || !annotationType.getName().endsWith("$List")) {
            return false;
        }

        Method method = ReflectionUtils.findMethod(annotationType, ANNOTATION_VALUE_KEY);

        if (method == null) {
            return false;
        }

        Class<?> returnType = method.getReturnType();

        if (!returnType.isArray()) {
            return false;
        }

        boolean equals = (returnType.getComponentType().getName() + "$List")
                .contentEquals(annotationType.getName());

        if (equals
                && isDoConsume
                && consumer != null) {
            Annotation[] result = (Annotation[]) ReflectionUtils.invokeMethod(method, annotation);
            if (result != null) {
                Stream.of(result).filter(Objects::nonNull).forEachOrdered(consumer);
            }
        }

        return equals;
    }

    /**
     * 核心方法
     * <p>
     * 第二层处理字段或是属性 关联的 组件集合
     * <p>
     * 本方法主要对多个注解进行分解，根据数据类型进行分解
     *
     * @param bean
     * @param fieldOrMethod
     * @param varAnnotations
     * @param name
     * @param varType
     * @param value
     */
    protected void processAttr(final Object bean, Object fieldOrMethod, Annotation[] varAnnotations, final String name, Class<?> varType, Object value) {

//        if (isIgnore(varAnnotations)) {
//            return;
//        }

        //如果没有类型，默认获取值的类型
        if ((varType == null || varType == Object.class) && value != null) {
            varType = value.getClass();
        }

        if (varType == null && value == null) {
            logger.warn(" *** processAttr " + name + " varType is null and value is null.");
        }

        final boolean isIterable = (varType != null && Iterable.class.isAssignableFrom(varType));
        final boolean isArray = (varType != null && varType.isArray());

        final List<Annotation> daoAnnotations = new ArrayList<>(5);

        final AtomicBoolean isNotDaoAnnotation = new AtomicBoolean(true);

        //合并组件的闭包
        final Consumer<Annotation> addAnnotationConsumer = annotation -> {

            //
            isNotDaoAnnotation.set(false);

            if (annotation instanceof CList) {
                CList clist = (CList) annotation;
                if (isValid(annotation, bean, name, value)) {
                    daoAnnotations.addAll(Arrays.asList(clist.value()));
                }
            } else if (annotation instanceof OrderByList) {
                if (isValid(annotation, bean, name, value)) {
                    daoAnnotations.addAll(Arrays.asList(((OrderByList) annotation).value()));
                }
            } else if (autoConsumerIfList(annotation, isValid(annotation, bean, name, value), daoAnnotations::add)) {
                //该if 条件不能去除
                //自动加入
            } else {
                daoAnnotations.add(annotation);
            }
        };

        //过滤字段级别注解
        findNeedProcessDaoAnnotations(fieldOrMethod, varAnnotations).forEach(addAnnotationConsumer);

        //如果字段没有注解，则尝试获取类上面的注解
        if (isNotDaoAnnotation.get() && daoAnnotations.isEmpty() && bean != null) {
            //扫描类级别注解
            findNeedProcessDaoAnnotations(fieldOrMethod, bean.getClass().getAnnotations()).forEach(addAnnotationConsumer);
        }

        //如果没有注解
        if (isNotDaoAnnotation.get() && daoAnnotations.isEmpty()) {

            boolean complexType = (findPrimitiveValue(varAnnotations) == null) && isComplexType(varType, value);

            if (complexType) {
                //递归加入条件
                reAppendByQueryObj(value);
            } else {
                //如果没有注解，不是复杂类型，则默认为等于查询
                daoAnnotations.add(getAnnotation(Eq.class));
            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////
        Class<?> eleType = null;

        if (isArray) {
            // eleType = varType.getComponentType();
        } else if (isIterable) {
            // ResolvableType.forInstance(bean).getGenerics()
        }

        for (Annotation annotation : daoAnnotations) {

            String newName = tryGetJpaEntityFieldName(annotation, tryGetEntityClass(annotation), name);

            //todo 是否尝试转换名称，表达式转换名称，支持 Spel
            newName = evalTextByThreadLocal(newName);

            //如果是扩展参数的操作 或是 不是迭代类型
            Op op = getOp(annotation);

            if (
                //空值直接忽略迭代
                    value == null
                            //如果没有操作 忽略迭代
                            || op == null
                            || (!isArray && !isIterable)
                            //如果是扩展参数的操作，如 IN NotIn Between等
                            || op.isExpandParamValue()
                            //如果是不需要参的操作，如 IS NULL，IS NOT NULL
                            || !op.isNeedParamExpr()) {

                if (isValid(annotation, bean, name, value)) {
                    processAttrAnno(bean, fieldOrMethod, varAnnotations, newName, varType, value, annotation);
                }

            } else {
                //可迭代参数
                Iterable<?> iterableData = isArray ? Arrays.asList((Object[]) value) : (Iterable<?>) value;

                for (Object paramValue : iterableData) {

                    Class<?> newVarType = (paramValue == null) ? null : (paramValue == value ? varType : paramValue.getClass());

                    //迭代循环
                    if (isValid(annotation, bean, name, paramValue)) {
                        processAttrAnno(bean, fieldOrMethod, varAnnotations, newName, newVarType, paramValue, annotation);
                    }

                }
            }
        }

    }

    protected static String trim(String str) {
        return str == null ? null : str.trim();
    }


    protected Class<?> tryGetEntityClass(Annotation annotation) {

        String domainAlias = ClassUtils.getValue(annotation, E_C.domain, false);

        if (!hasText(domainAlias)
                || domainAlias.trim().equalsIgnoreCase(trim(alias))) {
            return entityClass;
        }

        return aliasMap.get(domainAlias);
    }


    protected boolean isPackageStartsWith(String packageName, Annotation opAnnotation) {
        return opAnnotation != null && opAnnotation.annotationType().getName().startsWith(packageName);
    }

    /**
     * 单个属性处理方法，非常重要
     *
     * <p>
     * 默认只处理查询条件
     *
     * @param bean
     * @param fieldOrMethod
     * @param varAnnotations
     * @param name
     * @param varType
     * @param value
     * @param opAnnotation
     * @return 是否继续处理，true继续.false则停止
     */
    public void processAttrAnno(final Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        //如果不是条件注解则忽略
        //但是允许空opAnnotation为 null，往下走
        //支持处理 where 条件的注解

//        if (isIgnore(varAnnotations)) {
//            return;
//        }

        if (QueryAnnotationUtil.isSamePackage(opAnnotation, Eq.class)) {
            //处理where条件
            processWhereCondition(bean, varType, name, value, findPrimitiveValue(varAnnotations), opAnnotation);
        }

    }


    protected PrimitiveValue findPrimitiveValue(Annotation... varAnnotations) {
        return findFirstMatched(varAnnotations, PrimitiveValue.class);
    }

    public boolean isIgnore(Annotation[] varAnnotations) {
        return findFirstMatched(varAnnotations, Ignore.class) != null;
    }


    /**
     * 验证查询对象是否满足要求
     *
     * @param bean
     * @param name
     * @param value
     * @param validator
     */
    protected void verifyGroupValidation(Object bean, String name, Object value, Validator validator) {

        if (validator != null
                && hasText(validator.expr())) {
            //如果验证识别
            if (!evalTrueExpr(bean, value, name, validator.expr())) {
                throw new StatementBuildException(bean.getClass() + " group verify fail: "
                        + validator.promptInfo() + " on field " + name, validator.promptInfo());
            }
        }

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void appendToWhere(String expr, Object value, boolean addToFirst) {

        if (hasText(expr)
                && whereExprRootNode.currentNode().add(addToFirst, expr)) {
            if (addToFirst) {
                whereParamValues.add(0, value);
            } else {
                whereParamValues.add(value);
            }
        }
    }


    /**
     * 通过简单的注解添加条件
     * 主要为 SimpleConditionBuilder 提供服务
     *
     * @param annotationType
     * @param name
     * @param value
     */
    protected void add(Class annotationType, String name, Object value) {

        if (annotationType == IsNotNull.class
                || annotationType == IsNull.class
                || !isNullOrEmptyTxt(value)) {

            processWhereCondition(null, null, name, value, null, QueryAnnotationUtil.getAnnotation(annotationType));

        }

    }


    /**
     * @param value
     * @return
     */
    private boolean isNullOrEmptyTxt(Object value) {
        return value == null
                || (value instanceof CharSequence && !hasText((CharSequence) value));
    }


    protected CB having(String expr, Object... paramValues) {
        throw new UnsupportedOperationException("appendHaving [" + expr + "]");
    }


    /**
     * 处理 where 条件
     *
     * @param bean
     * @param varType
     * @param name
     * @param value
     * @param primitiveValue
     * @param opAnnotation
     */
    protected void processWhereCondition(final Object bean, Class<?> varType, String name, Object value,
                                         PrimitiveValue primitiveValue, Annotation opAnnotation) {

        genExprAndProcess(bean, varType, name, value, primitiveValue, opAnnotation, (expr, holder) -> {

            Boolean having = ClassUtils.getValue(opAnnotation, E_C.having, false);

            //变成 having 字句，只针对 SelectDao 有效
            if (Boolean.TRUE.equals(having) && this instanceof SelectDao) {
                having(expr, holder.value);
            } else {
                where(expr, holder.value);
            }

        });

    }


    protected void genExprAndProcess(final Object bean, Class<?> varType, String name, Object paramValue,
                                     PrimitiveValue primitiveValue, Annotation opAnnotation,
                                     BiConsumer<String, ValueHolder<Object>> consumer) {

        boolean complexType = (primitiveValue == null) && isComplexType(varType, paramValue);

        ValueHolder<Object> holder = new ValueHolder<>(bean, name, paramValue);

        String expr = genConditionExpr(complexType, opAnnotation, name, holder);

        consumer.accept(expr, holder);

    }


    /**
     * 递归处理
     *
     * @param queryObj
     */
    private void reAppendByQueryObj(Object queryObj) {
        appendByQueryObj(queryObj);
    }


    /**
     * @param holder
     * @return
     */
    protected String buildSubQuery(ValueHolder<?> holder) {
        return ExprUtils.buildSubQuery(holder, getDao(), isNative(), this.context);
    }


    /**
     * 关键方法，根据注解生成SQL语句
     *
     * @param complexType
     * @param opAnno
     * @param name
     * @param holder
     * @return
     */
    protected String genConditionExpr(boolean complexType, Annotation opAnno, String name, final ValueHolder<?> holder) {

        C c = null;

        if (opAnno instanceof C) {
            c = (C) opAnno;
        }

        if (c == null) {

            //把其它注解转换为 C 注解

            Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(opAnno);

            if (!attributes.containsKey(E_C.op)) {
                attributes.put(E_C.op, Op.valueOf(opAnno.annotationType().getSimpleName()));
            }

            if (!attributes.containsKey(E_C.having)) {
                attributes.put(E_C.having, false);
            }

            if (!attributes.containsKey(E_C.not)) {
                attributes.put(E_C.not, false);
            }

            if (attributes.get(E_C.op) == null) {
                throw new StatementBuildException(opAnno + " not define Op");
            }

            c = AnnotationUtils.synthesizeAnnotation(attributes, C.class, null);
        }

        List<Map<String, ?>> fieldCtxs = this.buildContextValues(holder.root, holder.value, name);

        Function<String, String> domainFunc = (domain) -> tryEvalExprIfHasSeplExpr(holder.root, holder.value, name, domain, fieldCtxs);

        return ExprUtils.genExpr(c, name, complexType, getExpectFieldType(domainFunc.apply(c.domain()), name), holder, getParamPlaceholder(),

                //condition 求值回调
                expr -> evalTrueExpr(holder.root, holder.value, name, expr, fieldCtxs),

                domainFunc,

                //字段别名和转换回调 求值回调
                this::aroundColumnPrefix,

                this::buildSubQuery, fieldCtxs);
    }

    protected String getText(String text, String prefix, String suffix, String defaultV) {
        return hasText(text) ? prefix + text + suffix : defaultV;
    }

    protected String getText(String text, String defaultV) {
        return hasText(text) ? text : defaultV;
    }


    /**
     * 检查安全模式
     *
     * @param whereStatement
     */
    protected void checkSafeMode(String whereStatement) {

        if (this.isSafeMode()
                && (!isSafeLimit() || !hasText(whereStatement))) {

            //默认2个条件都要满足，要有查询条件，也要有数量限制

            //如果超出安全模式的限制
            throw new DaoSecurityException("dao safe mode no allow no where statement"
                    + "or no limit or limit over " + getDao().getSafeModeMaxLimit());
        }
    }

    /**
     * 获取查询条件
     *
     * @param action 动作
     * @return
     */
    protected final String genWhereStatement(EntityOption.Action action) {

        if (isDisable(action)) {
            throw new DaoSecurityException(" " + entityClass + " disable " + action + " action");
        }

        String whereStatement = getText(whereExprRootNode.toString(), " Where ", " ", " ");

        checkSafeMode(whereStatement);

        EntityOption entityOption = getEntityOption();

        if (entityOption == null) {
            return whereStatement;
        }

        whereExprRootNode.switchCurrentNodeToSelf();

        if (autoAppendOperationCondition) {

            if (this instanceof UpdateDao
                    && hasText(entityOption.updateCondition())) {
                appendToWhere(entityOption.updateCondition(), Collections.emptyList(), true);
            } else if (this instanceof DeleteDao
                    && hasText(entityOption.deleteCondition())) {
                appendToWhere(entityOption.deleteCondition(), Collections.emptyList(), true);
            }
        }

        //如果过滤逻辑删除的数据
        if (filterLogicDeletedData
                && hasLogicDeleteField(entityOption)) {

            String expr = genLogicDeleteExpr(entityOption, Op.NotEq);

            String propertyName = entityOption.logicalDeleteFieldName().trim();

            if (isNullable(entityClass, propertyName)) {
                expr = " ( " + aroundColumnPrefix(propertyName) + " IS NULL OR " + expr + " ) ";
            }

            appendToWhere(expr, convertLogicDeleteValue(entityOption), true);
        }

        return getText(whereExprRootNode.toString(), " Where ", " ", " ");
    }

    protected String genLogicDeleteExpr(EntityOption entityOption, Op op) {
        return aroundColumnPrefix(entityOption.logicalDeleteFieldName().trim()) + " " + op.getOperator() + " " + getParamPlaceholder();
    }

    protected Object convertLogicDeleteValue(EntityOption entityOption) {
        return ObjectUtil.convert(entityOption.logicalDeleteValue().trim(), QueryAnnotationUtil.getFieldType(entityClass, entityOption.logicalDeleteFieldName().trim()));
    }


    protected String getParamPlaceholder() {
//        return localParamPlaceholder != null ? localParamPlaceholder : getDao().getParamPlaceholder(isNative());
        return getDao().getParamPlaceholder(isNative());
    }

    protected MiniDao getDao() {
        return dao;
    }

    /**
     * @return
     */
    protected boolean isNative() {
        return this.nativeQL;
    }

    /**
     * @return
     */
    protected boolean setNative(boolean nativeQL) {

        if (canChangeNativeQL) {
            this.nativeQL = nativeQL;
        }

        return this.nativeQL;
    }

    public CB setCanChangeNativeQL(boolean canChangeNativeQL) {
        this.canChangeNativeQL = canChangeNativeQL;
        return (CB) this;
    }

    /**
     * 返回当前节点是否有效
     * 注意，如果注解为null，则默认为有效
     *
     * @param anno
     * @param root
     * @param value @return
     */
    protected boolean isValid(Annotation anno, Object root, String name, Object value) {

        //如果没有注解
        if (anno == null) {
            return true;
        }

        String conditionExpr = ClassUtils.getValue(anno, E_C.condition, false);

        //如果没有内容默认为true
        if (!hasText(conditionExpr)) {
            return true;
        }

        boolean isOK = evalTrueExpr(root, value, name, conditionExpr);


        Boolean require = ClassUtils.getValue(anno, E_C.require, false);

        //如果是必须的，但条件又不成立，则抛出异常
        if (Boolean.TRUE.equals(require) && !isOK) {
            throw new IllegalArgumentException(String.format("field [%s] is require, annotation [%s] condition[%s] must be true"
                    , name, anno.annotationType().getSimpleName(), conditionExpr));
        }

        return isOK;
    }


    /**
     * @param root
     * @param value
     * @param name
     * @param expr  如果 expr 为 null
     * @return
     */
    protected boolean evalTrueExpr(final Object root, Object value, String name, String expr) {
        return evalTrueExpr(root, value, name, expr, buildContextValues(root, value, name));
    }


    protected boolean evalTrueExpr(final Object root, Object value, String name, String expr, List<Map<String, ? extends Object>> contexts) {

        /**
         * 默认是无条件限制
         */
        if (!StringUtils.hasText(expr)) {
            return true;
        }

        expr = expr.trim();

        //优化性能
        if (C.NOT_EMPTY.equals(expr) || expr.equals("#" + C.NOT_EMPTY)
                || C.VALUE_NOT_EMPTY.equals(expr) || expr.equals("#" + C.VALUE_NOT_EMPTY)) {
            return ExprUtils.isNotEmpty(value);
        }

        if (C.VALUE_EMPTY.equals(expr) || expr.equals("#" + C.VALUE_EMPTY)) {
            return ExprUtils.isEmpty(value);
        }

        return evalExpr(root, value, name, expr, contexts);
    }

    /**
     * 表达式求值
     *
     * @param root
     * @param value
     * @param name
     * @param expr
     * @return
     */
    protected String tryEvalExprIfHasSeplExpr(Object root, Object value, String name, String expr, List<Map<String, ? extends Object>> baseContexts, Map<String, ? extends Object>... exMaps) {

        if (!hasText(expr)) {
            return expr;
        }

        if (expr.trim().startsWith(ExpressionType.SPEL_PREFIX)) {

            expr = expr.substring(ExpressionType.SPEL_PREFIX.length());

            return evalExpr(root, value, name, expr, baseContexts, exMaps);
        }

        return expr;
    }

    /**
     * 表达式求值
     *
     * @param root
     * @param value
     * @param name
     * @param expr
     * @param baseContexts 可以为空
     * @param exMaps
     * @param <T>
     * @return
     */
    protected <T> T evalExpr(final Object root, Object value, String name, String expr, List<Map<String, ?>> baseContexts, Map<String, ?>... exMaps) {

        try {

            if (baseContexts == null) {
                baseContexts = buildContextValues(root, value, name);
            }

            if (exMaps != null) {
                for (Map<String, ?> exMap : exMaps) {
                    if (exMap != null) {
                        baseContexts.add(exMap);
                    }
                }
            }

            return ExprUtils.evalSpEL(root, expr, baseContexts);

        } catch (Exception e) {
            throw new StatementBuildException(name + " expression [" + expr + "] eval fail," + e.getMessage(), e);
        }
    }


    /**
     * 开始逻辑分组
     *
     * @param bean
     * @param logicAnnotation
     * @param value
     */
    private void beginLogicGroup(Object bean, Annotation logicAnnotation, String name, Object value) {
        if (logicAnnotation != null
                && !(logicAnnotation instanceof END)) {
            beginLogic(logicAnnotation.annotationType().getSimpleName(), isValid(logicAnnotation, bean, name, value));
        }
    }

    protected void beginLogic(String op, boolean valid) {
        whereExprRootNode.beginGroup(op, valid);
    }

    protected void endLogic(boolean isContainLastField) {
        whereExprRootNode.endGroup(isContainLastField);
    }

    private void endLogicGroup(Object bean, Annotation logicAnnotation, Object value) {
        //如果遇到逻辑结束
        if (logicAnnotation instanceof END) {
            endLogic(((END) logicAnnotation).containCurrentField());
        }
    }


    /**
     * 获取属性的数据类型
     *
     * @param name
     * @return
     */
    //@todo
    protected Class<?> getExpectFieldType(String domain, String name) {

        Class<?> entityType = null;

        if (!hasText(domain)) {
            entityType = hasEntityClass() ? entityClass : null;
        } else {
            entityType = aliasMap.get(domain);
        }

        return getFieldType(entityType, name, ((field, type) -> {
            //如果是枚举，且是原生查询，需要转换为字符串或是整形
            if (isNative()
                    && type != null
                    && type.isEnum()
                    && field != null) {
                type = getDao().getEnumConvertType(field, type);
            }
            return type;
        }));

    }


    public List<Map<String, ? extends Object>> getDaoContextValues() {

        return Arrays.asList(
                DaoContext.getGlobalContext(),
                DaoContext.getThreadContext(),
                (this.context != null) ? this.context : Collections.EMPTY_MAP);

    }


    /**
     * @param root
     * @param value
     * @param fieldName
     * @return
     */
    public List<Map<String, ? extends Object>> buildContextValues(Object root, Object value, String fieldName) {

        List<Map<String, ? extends Object>> contextValues = new ArrayList<>();

        contextValues.addAll(getDaoContextValues());

        if (root instanceof Map) {
            contextValues.add(Map.class.cast(root));
        } else if (root != null) {
            //把对象
            contextValues.add(ObjectUtil.copyField2Map(root, null));
        }

        if (value instanceof Map) {
            contextValues.add(Map.class.cast(value));
        }

        final boolean notEmpty = ExprUtils.isNotEmpty(value);

        contextValues.add(MapUtils
                .put("_this", root)
                .put("_name", fieldName)
                .put("_fieldName", fieldName)
                .put("_val", value)
                .put("_fieldVal", value)
                .put(C.NOT_EMPTY, notEmpty)
                .put(C.VALUE_NOT_EMPTY, notEmpty)
                .put(C.VALUE_EMPTY, !notEmpty)
                .put("_isSelect", (this instanceof SelectDao))
                .put("_isUpdate", (this instanceof UpdateDao))
                .put("_isDelete", (this instanceof DeleteDao))
                .build());

        return contextValues;

    }

}
