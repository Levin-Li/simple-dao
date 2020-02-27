package com.levin.commons.dao.support;


import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import com.levin.commons.dao.annotation.misc.Validator;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.Having;
import com.levin.commons.dao.annotation.update.Immutable;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 条件构建器实现
 * 本类是一个非线程安全类，不要重复使用，应该重新创建使用。
 *
 * @param <T>
 * @param <C>
 */
public abstract class ConditionBuilderImpl<T, C extends ConditionBuilder>
        implements ConditionBuilder<C> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DELIMITER = " , ";

    protected javax.validation.Validator validator;

    protected Class<T> entityClass;

    protected String tableName;

    protected String alias;

    final List whereParamValues = new ArrayList(5);

    int rowStart = -1, rowCount = -1;

    private final boolean nativeQL;


    private ExprNode whereExprRootNode = new ExprNode(AND.class.getSimpleName(), true);

    private Map<String, Object> context;

    protected ParameterNameDiscoverer parameterNameDiscoverer;

    protected final List<TargetOption> targetOptionAnnoList = new ArrayList<>(5);


    private static final Object[] EMPTY_PARAM_VALUES = {};


    public static final String BASE_PACKAGE_NAME = Eq.class.getPackage().getName();

    public static final String LOGIC_PACKAGE_NAME = AND.class.getPackage().getName();


    protected boolean safeMode = true;


   // protected String localParamPlaceholder = null;


    protected ConditionBuilderImpl(boolean isNative) {

        this.nativeQL = isNative;
        this.entityClass = null;
        this.tableName = null;
        this.alias = null;

    }

    public ConditionBuilderImpl(Class<T> entityClass, String alias) {

        this.entityClass = entityClass;
        this.nativeQL = false;
        this.tableName = null;
        this.alias = (alias != null && entityClass != null) ? alias.trim() : null;

        if (entityClass == null)
            throw new IllegalArgumentException("entityClass is null");

//        if (!entityClass.isAnnotationPresent(Entity.class)) {
//            throw new IllegalArgumentException("entityClass is not a jpa Entity class");
//        }

    }

    public ConditionBuilderImpl(String tableName, String alias) {

        this.tableName = tableName;
        this.entityClass = null;
        this.nativeQL = true;
        this.alias = (alias != null && tableName != null) ? alias.trim() : null;

        if (tableName == null)
            throw new IllegalArgumentException("tableName is null");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public ParameterNameDiscoverer getParameterNameDiscoverer() {

        if (parameterNameDiscoverer == null) {
            parameterNameDiscoverer = new MethodParameterNameDiscoverer();
        }

        return parameterNameDiscoverer;
    }

    public C setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        return (C) this;
    }

    public javax.validation.Validator getValidator() {
        return validator;
    }

    public C setValidator(javax.validation.Validator validator) {
        this.validator = validator;
        return (C) this;
    }


    /**
     * 禁止安全模式
     * 在安全模式下，不允许无条件的更新或是删除
     */
    @Override
    public void disableSafeMode() {
        safeMode = false;
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

    /**
     * 获取参数占位符
     * <p/>
     * 后期考虑支持位置参数
     * 还有命名参数
     *
     * @param name
     * @return
     */
    protected String getParamPlaceholder(String name) {
        return StringUtils.hasText(name) ? ":" + name : getParamPlaceholder();
    }


    @Override
    public C setContext(Map<String, Object> context) {
        this.context = context;
        return (C) this;
    }


    public Map<String, Object> getDaoContextValues() {

        return QueryAnnotationUtil.copyMap(true, null,
                DaoContext.getGlobalContext(),
                DaoContext.getThreadContext(),
                this.context);
    }


    @Override
    public C and() {
        beginLogic(AND.class.getSimpleName(), true);
        return (C) this;
    }

    @Override
    public C or() {
        beginLogic(OR.class.getSimpleName(), true);
        return (C) this;
    }

    @Override
    public C end() {
        endLogic();
        return (C) this;
    }

    @Override
    public C limit(int rowStartPosition, int rowCount) {
        this.rowStart = rowStartPosition;
        this.rowCount = rowCount;
        return (C) this;
    }

    /**
     * @param rowStartPosition
     * @param rowCount
     * @return
     * @todo 目前只对查询有效，对于更新和删除语句无效(待优化)
     */
    public C range(int rowStartPosition, int rowCount) {
        return limit(rowStartPosition, rowCount);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public C where(String whereStatement, Object... paramValues) {

        this.whereExprRootNode.clear();
        this.whereParamValues.clear();

        this.appendWhere(whereStatement, paramValues);

        return (C) this;
    }

    @Override
    public C appendWhere(String conditionExpr, Object... paramValues) {

        append(conditionExpr, paramValues);

        return (C) this;
    }

    @Override
    public C appendWhere(Boolean isAppend, String conditionExpr, Object... paramValues) {

        if (Boolean.TRUE.equals(isAppend))
            append(conditionExpr, paramValues);

        return (C) this;
    }


    /**
     * 建议使用 appendByQueryObj替代
     *
     * @param queryObjs 可以值对象要求打上com.levin.commons.dao.annotation注解
     * @return
     * @see #appendByQueryObj
     * @see com.levin.commons.dao.annotation
     */
    @Override
    public C appendWhereByQueryObj(Object... queryObjs) {
        return appendByQueryObj(queryObjs);
    }

    @Override
    public C appendByQueryObj(Object... queryObjs) {

        walkObject(queryObjs);

        return (C) this;
    }


    @Override
    public C appendByMethodParams(Object bean, Method method, Object... args) {

        walkMethod(bean, method, args);

        return (C) this;
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
    public C appendByEL(String paramPrefix, Map<String, Object>... queryParams) {

        walkMap(paramPrefix, queryParams);

        return (C) this;
    }


    @Override
    public C appendByAnnotations(Boolean isAppend, @javax.validation.constraints.NotNull String attrName, Object attrValue, Class<? extends Annotation>... annoTypes) {

        if (Boolean.TRUE.equals(isAppend)) {
            processAttr(null, null, attrName, QueryAnnotationUtil.getAnnotations(annoTypes), null, attrValue);
        }

        return (C) this;
    }


    public int getRowStart() {
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    protected C setTargetOption(Object hostObj, TargetOption targetOption) {


        if (targetOption == null)
            return (C) this;

        //重复的不再处理
        if (targetOptionAnnoList.contains(targetOption))
            return (C) this;

        targetOptionAnnoList.add(targetOption);

        if (this.entityClass == null
                && targetOption.entityClass() != null
                && targetOption.entityClass() != Void.class)
            this.entityClass = targetOption.entityClass();

        if (!StringUtils.hasText(this.tableName)
                && StringUtils.hasText(targetOption.tableName()))
            this.tableName = targetOption.tableName();


        if (StringUtils.hasText(targetOption.fromStatement())) {
            setFromStatement(targetOption.fromStatement());
        }

        if (!StringUtils.hasText(this.alias)
                && StringUtils.hasText(targetOption.alias()))
            this.alias = targetOption.alias();

        ValueHolder holder = new ValueHolder(hostObj, null);

        this.appendWhere(this.doReplace(targetOption.fixedCondition(), false, holder), holder.hasValue() ? holder.value : EMPTY_PARAM_VALUES);

        //设置limit，如果原来没有设置
        if (rowStart <= 0 && targetOption.startIndex() > 0) {
            rowStart = targetOption.startIndex();
        }

        if (rowCount <= 0 && targetOption.maxResults() > 0) {
            rowCount = targetOption.maxResults();
        }


        return (C) this;
    }


    protected void setPaging(Object queryObj) {

        //如果不是分页对象
        if (!(queryObj instanceof Paging)) {
            return;
        }

        Paging paging = (Paging) queryObj;

        //页面数大于0才有效
        if (paging.getPageSize() > 0) {

            rowCount = paging.getPageSize();

            if (paging.getPageIndex() > 0) {
                rowStart = (paging.getPageIndex() - 1) * paging.getPageSize();
            }

        }
    }


    protected void setFromStatement(String fromStatement) {
        throw new UnsupportedOperationException(getClass().getName() + " setFromStatement");
    }

//////////////////////////////////////////////

    protected void beforeWalkMethod(Object bean, Method method, Object[] args) {

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

    protected void afterWalkMethod(Object bean, Method method, Object[] args) {

    }

    protected void walkMethod(Object bean, Method method, Object[] args) {

        //如果是忽略的方法
        if (method.getAnnotation(Ignore.class) != null)
            return;

        Class<?>[] parameterTypes = method.getParameterTypes();

        String[] parameterNames = getParameterNameDiscoverer().getParameterNames(method);

        if (parameterNames == null || parameterTypes == null
                || parameterNames.length != parameterTypes.length)
            throw new IllegalStateException("method [" + method + "] can't get param name");

        for (String parameterName : parameterNames) {
            if (parameterName == null || !StringUtils.hasText(parameterName))
                throw new IllegalStateException("method [" + method + "] can't get param name");
        }

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        beforeWalkMethod(bean, method, args);


        for (int i = 0; i < parameterTypes.length; i++) {

            String pName = parameterNames[i];

            Annotation[] varAnnotations = parameterAnnotations[i];
            Class<?> pType = ResolvableType.forMethodParameter(method, i).resolve(parameterTypes[i]);
            Object value = args[i];

            processAttr(bean, method, pName, varAnnotations, pType, value);

        }

        //之前之后
        afterWalkMethod(bean, method, args);

    }

    /**
     * 解析对象所有的属性，过滤并调用回调
     *
     * @param queryObjs
     */
    public void walkObject(Object... queryObjs) {
        walkObject(null, queryObjs);
    }

    /**
     * 解析对象所有的属性，过滤并调用回调
     *
     * @param queryObjs
     */
    public void walkObject(AttrCallback callback, Object... queryObjs) {

        if (queryObjs == null)
            return;

        for (Object queryValueObj : queryObjs) {

            if (queryValueObj == null) {
                continue;
            }

            Class<?> typeClass = queryValueObj.getClass();

            if (typeClass.isPrimitive()
                    || QueryAnnotationUtil.isRootObjectType(typeClass)
                    || QueryAnnotationUtil.isArray(typeClass)
                    || QueryAnnotationUtil.isIgnore(typeClass)
                    || typeClass.isAnnotation()
                    ) {
                continue;
            }

            //没有回调时，表示本地调用
            if (callback == null) {
                //尝试设置分页
                setPaging(queryValueObj);

                //尝试设置查询目标实体
                setTargetOption(queryValueObj, typeClass.getAnnotation(TargetOption.class));
            }

            //特别处理
            if (queryValueObj instanceof Map) {
                walkMap("", (Map) queryValueObj);
                continue;
            }

            List<Field> fields = QueryAnnotationUtil.getCacheFields(typeClass);

            ResolvableType rootType = ResolvableType.forType(typeClass);

            //开始处理字段
            for (Field field : fields) {

                //忽略字段
                if (QueryAnnotationUtil.isIgnore(field)) {
                    continue;
                }

                ResolvableType fieldRT = ResolvableType.forField(field, rootType);


                Class<?> fieldRealType = fieldRT.resolve(field.getType());

                field.setAccessible(true);

                try {
                    if (callback != null) {
                        boolean isContinue = callback.onAction(queryValueObj
                                , field, field.getName(), field.getAnnotations()
                                , fieldRealType,
                                field.get(queryValueObj));

                        //如果不再处理，则跳出字段处理
                        if (!isContinue)
                            break;

                    } else {
                        processAttr(queryValueObj
                                , field, field.getName(), field.getAnnotations()
                                , fieldRealType,
                                field.get(queryValueObj));
                    }
                } catch (Exception e) {
                    throw new StatementBuildException(typeClass + " 处理注解失败，字段:" + field + "", e);
                }
            }


            //拷贝对象的字段，可能会被作为命名的查询参数

            whereParamValues.add(QueryAnnotationUtil.copyMap(true, null, ObjectUtil.copyField2Map(queryValueObj, null)));

        }
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

        if (queryParams == null)
            return;

        boolean hasPrefix = StringUtils.hasText(paramPrefix);

        Map<String, Annotation> annotationMap = QueryAnnotationUtil.getAllAnnotations();

        final String notPrefix = Not.class.getSimpleName() + "_";

        for (Map<String, Object> queryParam : queryParams) {

            for (Map.Entry<String, Object> entry : queryParam.entrySet()) {

                String name = entry.getKey();
                Object paramValue = entry.getValue();

                if (hasPrefix) {
                    //如果不是有效的属性，则忽略
                    if (!name.startsWith(paramPrefix))
                        continue;
                    //去除前缀
                    name = name.substring(paramPrefix.length());
                }

                Not notOp = null;

                //是否包括非的操作
                if (name.startsWith(notPrefix)) {
                    notOp = (Not) annotationMap.get(Not.class.getSimpleName());
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
                    if (name.length() > idx + 1)
                        name = name.substring(idx + 1);
                    else {
                        //logger.trace("");
                        continue;
                    }
                } else {
                    //默认是等于的操作
                    opAnno = annotationMap.get(Eq.class.getSimpleName());
                }

                //如果属性名为null
                if (!StringUtils.hasText(name)) {
                    continue;
                }

                //如果是忽略的条件
                if (opAnno instanceof Ignore) {
                    continue;
                }

//                processWhereCondition(name, paramValue, notOp, opAnno);


                Annotation[] varAnnotations = new Annotation[notOp != null ? 2 : 1];


                varAnnotations[0] = opAnno;

                if (notOp != null) {
                    varAnnotations[1] = notOp;
                }

                processAttr(queryParam, null, name, varAnnotations,
                        paramValue != null ? paramValue.getClass() : null, paramValue);

            }
        }
    }


    /**
     * 处理单个属性或是方法参数的注解
     *
     * @param bean
     * @param fieldOrMethod
     * @param name
     * @param varAnnotations
     * @param attrType
     * @param value
     */
    public void processAttr(Object bean, Object fieldOrMethod, String name, Annotation[] varAnnotations, Class<?> attrType, Object value) {

        //如果是包括忽略注解，则直接忽略
        if (QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, Ignore.class) != null)
            return;

        //自动进行字段的转换
        //@todo
        //  value = tryAutoConvert(name, QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, PrimitiveValue.class), attrType, value);


        beginLogicGroup(bean, QueryAnnotationUtil.getLogicAnnotation(name, varAnnotations), name, value);

        try {

            //如果是忽略的类型
            if (attrType != null && attrType.getAnnotation(Ignore.class) != null)
                return;

            //当前节点是否有效
            if (!whereExprRootNode.getCurrentNode().isValid())
                return;

            processAttr(bean, fieldOrMethod, varAnnotations, name, attrType, value);

        } finally {
            endLogicGroup(bean, QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, END.class), value);
        }

        //结束逻辑分组
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

        List<Annotation> result = new ArrayList<>(3);

        if (varAnnotations != null) {
            for (Annotation annotation : varAnnotations) {

                if (annotation == null
                        || annotation instanceof Not
                        || annotation instanceof PrimitiveValue
                        || annotation instanceof OrderBy
                        || annotation instanceof Immutable
                        || annotation instanceof Validator
                        || annotation instanceof Having
                        || annotation instanceof Ignore)
                    continue;

                //如果注解的类是在这"com.levin.commons.dao.annotation" 包下，并且不是逻辑操作注解
                //特别关键的判断条件

                String clsName = annotation.annotationType().getName();

                if (clsName.startsWith(BASE_PACKAGE_NAME)
                        && !clsName.startsWith(LOGIC_PACKAGE_NAME))
                    result.add(annotation);
            }
        }

        return result;
    }


    /**
     * @param entityAttrName
     * @param value
     * @param varAnnotations
     */
    protected void processAttr(String entityAttrName, Object value, Annotation... varAnnotations) {
        processAttr(null, null, varAnnotations, entityAttrName, null, value);
    }

    /**
     * 处理原子属性
     * 关键处理方法
     * <p/>
     * 对每一个字段，对每一个注解循环处理
     *
     * @param bean
     * @param fieldOrMethod
     * @param varAnnotations
     * @param name
     * @param varType
     * @param value
     */
    protected void processAttr(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value) {

        List<Annotation> daoAnnotations = findNeedProcessDaoAnnotations(fieldOrMethod, varAnnotations);

        //如果没有注解
        if (daoAnnotations.size() == 0) {
            //如果字段上没有需要处理的注解
            processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, null);
        } else {
            for (Annotation opAnnotation : daoAnnotations) {
                //如果是有效的节点和有效注解条件
                if (isValid(opAnnotation, bean, name, value)) {
                    //转换名称
                    name = QueryAnnotationUtil.getPropertyName(opAnnotation, name);

                    boolean isContinue = processAttrAnno(bean, fieldOrMethod, varAnnotations, name, varType, value, opAnnotation);

                    //***重要逻辑***  是继续处理这个字段上的其它注解
                    if (!isContinue)
                        break;
                }
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
     * @return 是否继续处理，true继续.false则停止
     */
    public boolean processAttrAnno(Object bean, Object fieldOrMethod, Annotation[] varAnnotations, String name, Class<?> varType, Object value, Annotation opAnnotation) {

        //如果不是条件注解则忽略
        //但是允许空opAnnotation为 null，往下走
        //支持处理 where 条件的注解

        if (opAnnotation != null
                && !opAnnotation.annotationType().getPackage().getName().equals(BASE_PACKAGE_NAME)) {
            return true;
        }


        //如果非操作符
        if (opAnnotation instanceof Not)
            return true;

        Not notAnnotation = QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, Not.class);


        verifyGroupValidation(bean, name, value, QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, Validator.class));


        PrimitiveValue primitiveValue = QueryAnnotationUtil.getFirstMatchedAnnotation(varAnnotations, PrimitiveValue.class);

        //处理where条件
        processWhereCondition(bean, varType, name, value, primitiveValue, notAnnotation, opAnnotation);

        return true;
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
                && hasContent(validator.expr())) {

            boolean ok = evalExpr(bean, value, validator.expr());

            if (!ok) {
                throw new StatementBuildException(bean.getClass() + " group valid fail: " + validator.promptInfo() + " on field " + name, validator.promptInfo());
            }

        }

    }


    /**
     * 参数的属性名称中带有查询注释说明
     * <p/>
     * 如：属性名Q_Not_Like_name  值 llw，表f示会生成查询条件 name not like '%llw%'
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
     * param.put("Q_NotNull_date8", new Date());
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
     * @param paramPrefix
     * @param queryParams
     * @return
     */
    @Override
    public C appendWhereByEL(String paramPrefix, Map<String, Object>... queryParams) {

        walkMap(paramPrefix, queryParams);

        return (C) this;
    }

    /**
     * 增加单个参数
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入条件
     * @return
     */

    @Override
    public C appendWhereEquals(String entityAttrName, Object paramValue) {

        add(Eq.class, entityAttrName, paramValue);

        return (C) this;
    }


    @Override
    public C appendBetween(String entityAttrName, Object... paramValues) {

        // processWhereCondition(entityAttrName, paramValues, null, QueryAnnotationUtil.getAnnotation(Between.class));

        add(Between.class, entityAttrName, paramValues);

        return (C) this;
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public C appendWhereIn(String entityAttrName, Object... paramValues) {

        // processWhereCondition(entityAttrName, paramValues, null, QueryAnnotationUtil.getAnnotation(In.class));

        add(In.class, entityAttrName, paramValues);

        return (C) this;
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public C appendWhereNotIn(String entityAttrName, Object... paramValues) {
        //processWhereCondition(entityAttrName, paramValues, null, QueryAnnotationUtil.getAnnotation(NotIn.class));

        add(NotIn.class, entityAttrName, paramValues);

        return (C) this;
    }

    /**
     * like %keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C appendWhereContains(String entityAttrName, String keyword) {

        //     processWhereCondition(entityAttrName, keyword, null, QueryAnnotationUtil.getAnnotation(Contains.class));

        add(Contains.class, entityAttrName, keyword);

        return (C) this;
    }

    /**
     * like %keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C appendWhereStartsWith(String entityAttrName, String keyword) {
        //    processWhereCondition(entityAttrName, keyword, null, QueryAnnotationUtil.getAnnotation(StartsWith.class));


        add(StartsWith.class, entityAttrName, keyword);

        return (C) this;
    }

    /**
     * like %keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C appendWhereEndsWith(String entityAttrName, String keyword) {
        //  processWhereCondition(entityAttrName, keyword, null, QueryAnnotationUtil.getAnnotation(EndsWith.class));

        add(EndsWith.class, entityAttrName, keyword);

        return (C) this;
    }

    /**
     * is null
     *
     * @param entityAttrName 如 name
     * @return
     */
    @Override
    public C isNull(String entityAttrName) {

        add(Null.class, entityAttrName, null);

        return (C) this;
    }

    /**
     * is not null
     *
     * @param entityAttrName 如 name
     * @return
     */
    @Override
    public C isNotNull(String entityAttrName) {

        add(NotNull.class, entityAttrName, null);

        return (C) this;
    }


    @Override
    public C isNullOrEq(String entityAttrName, Object paramValue) {

        appendByAnnotations(true, entityAttrName, paramValue, OR.class, Null.class, Eq.class, END.class);

        return (C) this;
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
    public C eq(String entityAttrName, Object paramValue) {

        add(Eq.class, entityAttrName, paramValue);

        return (C) this;
    }

    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public C notEq(String entityAttrName, Object paramValue) {

        add(NotEq.class, entityAttrName, paramValue);

        return (C) this;
    }

    /**
     * >
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public C gt(String entityAttrName, Object paramValue) {

        add(Gt.class, entityAttrName, paramValue);

        return (C) this;
    }

    /**
     * <
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public C lt(String entityAttrName, Object paramValue) {

        add(Lt.class, entityAttrName, paramValue);

        return (C) this;
    }

    /**
     * >=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public C gte(String entityAttrName, Object paramValue) {

        add(Gte.class, entityAttrName, paramValue);

        return (C) this;
    }

    /**
     * <=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    @Override
    public C lte(String entityAttrName, Object paramValue) {

        add(Lte.class, entityAttrName, paramValue);

        return (C) this;
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
    public C between(String entityAttrName, Object... paramValues) {

        add(Between.class, entityAttrName, paramValues);

        return (C) this;
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public C in(String entityAttrName, Object... paramValues) {

        add(In.class, entityAttrName, paramValues);

        return (C) this;
    }

    /**
     * field in (?...)
     *
     * @param entityAttrName
     * @param paramValues
     * @return
     */
    @Override
    public C notIn(String entityAttrName, Object... paramValues) {

        add(NotIn.class, entityAttrName, paramValues);

        return (C) this;
    }


    /**
     * exist (sub query)
     *
     * @param queryObjs
     * @return
     */
    @Override
    public C exists(Object... queryObjs) {


        return (C) this;
    }

    /**
     * not exist (sub query)
     *
     * @param queryObjs
     * @return
     */
    @Override
    public C notExists(Object... queryObjs) {

        return (C) this;
    }

    /**
     * like %keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C contains(String entityAttrName, String keyword) {

        add(Contains.class, entityAttrName, keyword);

        return (C) this;
    }

    /**
     * like keyword%
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C startsWith(String entityAttrName, String keyword) {

        add(StartsWith.class, entityAttrName, keyword);

        return (C) this;
    }

    /**
     * like %keyword
     *
     * @param entityAttrName
     * @param keyword
     * @return
     */
    @Override
    public C endsWith(String entityAttrName, String keyword) {

        add(EndsWith.class, entityAttrName, keyword);

        return (C) this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void append(String expr, Object value) {

        if (StringUtils.hasText(expr)
                && whereExprRootNode.addToCurrentNode(expr)) {
            whereParamValues.add(value);
        }

    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    protected void add(Class annotationType, String name, Object value) {
        processWhereCondition(null, null, name, value, null, null
                , QueryAnnotationUtil.getAnnotation(annotationType));
    }


    protected void addNot(Class annotationType, String name, Object value) {
        processWhereCondition(null, null, name, value, null, (Not) QueryAnnotationUtil.getAnnotation(Not.class)
                , QueryAnnotationUtil.getAnnotation(annotationType));
    }

    protected void processWhereCondition(String name, Object value, Not notAnnotation, Annotation opAnnotation) {
        processWhereCondition(null, null, name, value, null, notAnnotation, opAnnotation);
    }

    /**
     * 关键方法
     * <p>
     * 如果是数组，必须要求不存在原子元素，并且不为空数组，并且元素不都是Null
     *
     * @param varType
     * @param value
     * @return
     */
    protected boolean isComplexType(Class<?> varType, Object value) {

        if (varType == null && value != null) {

            // 是数组并且有原子元素
            if (QueryAnnotationUtil.isArrayAndExistPrimitiveElement(value)) {
                return false;
            }

            varType = value.getClass();
        }

        return varType != null
                && varType.getAnnotation(PrimitiveValue.class) == null
                && !QueryAnnotationUtil.isPrimitive(varType);
    }


    protected void processWhereCondition(Object bean, Class<?> varType, String name, Object value, PrimitiveValue primitiveValue, Not notAnnotation, Annotation opAnnotation) {


        boolean isNullTag = (opAnnotation instanceof Null || opAnnotation instanceof NotNull);

        if (isNullTag) {

            //如果有notnull 或是null 那么值被忽略，会自动转换成sql表达式: is null 和is not null

//            if (notAnnotation != null) {
//                throw new StatementBuildException("Null or NotNull can't together with Not");
//            }

            append(processNotTag(bean, notAnnotation, processNullTag(bean, name, opAnnotation)), new Object[]{});

            // processNullTag(name, opAnnotation);

        } else if (value != null) {

            boolean enableEmptyString =
                    (context != null && Boolean.TRUE.equals(context.get(DaoContext.ENABLE_EMPTY_STRING_QUERY)))
                            || Boolean.TRUE.equals(DaoContext.getVar(DaoContext.ENABLE_EMPTY_STRING_QUERY, Boolean.FALSE));

            if (!enableEmptyString
                    && value instanceof CharSequence
                    && ((CharSequence) value).length() < 1) {
                //忽略空串
                return;
            }

            //如果是复杂对象，即对象或是对象数组（数组元素为非原子对象）
            boolean complexType = (primitiveValue == null) && isComplexType(varType, value);

            //尝试转换值
            //@modify by llw,20170829，修复 Null和NotNull注解时，并不使用属性值，所以无需进行值转换
            if (!complexType) {
                value = tryToConvertValue(name, value);
            }

            ValueHolder holder = new ValueHolder(bean, value);

            String conditionExpr = genConditionExpr(complexType, name, holder, opAnnotation);

            appendWhere(processNotTag(bean, notAnnotation, conditionExpr), holder.value);

        } else {
            //忽略空值的注解
        }

    }

    Object tryAutoConvert(String name, PrimitiveValue primitiveValue, Class<?> varType, Object value) {

        boolean enableEmptyString =
                (context != null && Boolean.TRUE.equals(context.get(DaoContext.ENABLE_EMPTY_STRING_QUERY)))
                        || Boolean.TRUE.equals(DaoContext.getVar(DaoContext.ENABLE_EMPTY_STRING_QUERY, Boolean.FALSE));

        if (!enableEmptyString
                && value instanceof CharSequence
                && ((CharSequence) value).length() < 1) {
            //忽略空串
            return value;
        }

        //如果是复杂对象，即对象或是对象数组（数组元素为非原子对象）
        boolean complexType = (primitiveValue == null) && isComplexType(varType, value);

        //尝试转换值
        //@modify by llw,20170829，修复 Null和NotNull注解时，并不使用属性值，所以无需进行值转换
        if (!complexType) {
            value = tryToConvertValue(name, value);
        }

        return value;
    }


    String processNotTag(Object rootBean, Not notAnnotation, String expr) {
        //如果有非操作，则把已经生成的语句用非语句包围起来
        if (StringUtils.hasText(expr) && notAnnotation != null)
            expr = genConditionExpr(false, notAnnotation, "", new ValueHolder(rootBean, expr));
        return expr;
    }


    protected String processNullTag(Object rootBean, String name, Annotation opAnno) {
        return genConditionExpr(false, opAnno, name, new ValueHolder(rootBean, null));
    }


    protected boolean hasPrimitiveAnno(Annotation... annotations) {
        return QueryAnnotationUtil.getFirstMatchedAnnotation(annotations, PrimitiveValue.class) != null;
    }

    /**
     * 生成条件语句
     *
     * @param holder
     * @param name
     * @param op
     * @return
     */
    protected String genConditionExpr(boolean complexType, String name, ValueHolder holder, Annotation op) {

        String expr = "";

        if (op != null) {
            expr = genConditionExpr(complexType, op, name, holder);
        } else {
            //如果没有条件，默认就是相等
            //如果是复杂对象，则递归处理
            if (complexType) {

                reAppendByQueryObj(holder.value);

            } else if (QueryAnnotationUtil.isNotEmptyArray(holder.value)) {
                throw new StatementBuildException(name + "属性错误：基本类型的数组必须声明注解");
            } else {
                //默认是相等操作
                expr = aroundColumnPrefix(name) + " = " + getParamPlaceholder();
//                expr = genConditionExpr(complexType, op, name, holder);
            }
        }


        return expr;
    }

    /**
     * 递归处理
     *
     * @param queryObj
     */
    private void reAppendByQueryObj(Object queryObj) {
        if (queryObj != null) {
            if (queryObj.getClass().isArray())
                appendByQueryObj((Object[]) queryObj);
            else
                appendByQueryObj(queryObj);
        }
    }

    /**
     * 尝试自动加上小括号，并自动处理重复小括号的问题
     *
     * @param expr
     * @return
     */
    protected String autoAroundParentheses(String prefix, String expr, String suffix) {

        expr = StringUtils.trimWhitespace(expr);

        if (expr.length() > 0) {

            prefix = StringUtils.trimAllWhitespace(prefix);
            suffix = StringUtils.trimAllWhitespace(suffix);

            //如果
            if (!prefix.endsWith("(")
                    && !suffix.startsWith(")")
                    && !expr.startsWith("(")
                    && !expr.endsWith(")")) {

                expr = "( " + expr + " )";

            }

            expr = prefix + " " + expr + " " + suffix;
        }

        return expr;
    }

    /**
     * @param holder
     * @return
     */
    protected String buildSubQuery(ValueHolder holder) {

        SelectDaoImpl selectDao = new SelectDaoImpl(this.isNative(), getDao());

       // selectDao.localParamPlaceholder = this.localParamPlaceholder;

        Object queryObj = holder.value;

        if (queryObj != null) {
            if (queryObj.getClass().isArray())
                selectDao.appendByQueryObj((Object[]) queryObj);
            else
                selectDao.appendByQueryObj(queryObj);
        }

        String subStatement = selectDao.genFinalStatement();

        if (StringUtils.hasText(subStatement)) {
            holder.value = selectDao.genFinalParamList();
        } else {
            holder.value = new Object[0];
        }

        return subStatement;
    }


    private String getSubQuery(Annotation opAnno) {

        try {
            return (String) opAnno.getClass().getDeclaredMethod("subQuery").invoke(opAnno);
        } catch (Exception e) {
        }

        return "";
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
    private String genConditionExpr(boolean complexType, Annotation opAnno, String name, ValueHolder holder) {

        if (opAnno == null)
            return "";

        try {

            String op = (String) opAnno.getClass().getDeclaredMethod("op").invoke(opAnno);
            String prefix = (String) opAnno.getClass().getDeclaredMethod("prefix").invoke(opAnno);
            String suffix = (String) opAnno.getClass().getDeclaredMethod("suffix").invoke(opAnno);

            if (opAnno instanceof Not) {
                //忽略左操作数
                //示例：NOT (u.name  =   ? )

                return op + getText(prefix, "(") + holder.value + getText(suffix, ")");

            } else if (opAnno instanceof Like || opAnno instanceof NotLike
                    || opAnno instanceof Contains
                    || opAnno instanceof StartsWith || opAnno instanceof EndsWith) {
                //自动加上
                //转换为字符串
                holder.value = "" + prefix + holder.value + suffix;

                return aroundColumnPrefix(name) + " " + op + " " + getParamPlaceholder();

            } else if (opAnno instanceof In || opAnno instanceof NotIn) {

                //是否过滤空值
                //JPA QL 是默认支持 in 语法
                if ((opAnno instanceof In && ((In) opAnno).filterNullValue())
                        || (opAnno instanceof NotIn && ((NotIn) opAnno).filterNullValue())) {
                    holder.value = QueryAnnotationUtil.filterNullValue(holder.value, true);
                }

                //JPA QL 是默认支持 in 语法，为了兼容SQL，拆成多个问号
                int eleCount = QueryAnnotationUtil.eleCount(holder.value);

                if (eleCount == 0) {
                    return "";
                }

                String subQuery = getSubQuery(opAnno);

                //最优先使用自定义的子查询
                if (StringUtils.hasText(subQuery)) {
                    return aroundColumnPrefix(name) + " " + op + " " + autoAroundParentheses(prefix, doReplace(subQuery, false, holder), suffix);
                }

                //如果是复杂对象，默认为子查询
                if (complexType) {
                    return aroundColumnPrefix(name) + " " + op + " " + autoAroundParentheses(prefix, buildSubQuery(holder), suffix);
                }

                return aroundColumnPrefix(name) + " " + op + " " + prefix + genParamExpr(",", getParamPlaceholder(), eleCount) + suffix;

            } else if (opAnno instanceof Exists || opAnno instanceof NotExists) {

                String subQuery = getSubQuery(opAnno);

                //最优先使用自定义的子查询
                if (StringUtils.hasText(subQuery)) {
                    return " " + op + " " + autoAroundParentheses(prefix, doReplace(subQuery, false, holder), suffix);
                }

                if (!complexType) {
                    throw new StatementBuildException(name + "注解使用有误,Exists或NotExists 注解必须指定'subQuery'值，或是应用于查询对象上");
                }

                return " " + op + " " + autoAroundParentheses(prefix, buildSubQuery(holder), suffix);

            } else if (opAnno instanceof NotNull || opAnno instanceof Null) {
                //忽略参数值
                holder.value = new Object[]{};
                return getText(prefix, "") + aroundColumnPrefix(name) + getText(suffix, "") + " " + op + " ";
            } else if (opAnno instanceof Between) {
                //是否过滤空值
                //JPA QL 是默认支持 in 语法

                if (complexType)
                    throw new StatementBuildException(name + "注解使用有误,Between 注解只允许使用在原子类型数组上");

                Between between = (Between) opAnno;

                if ((between.filterNullValue())) {
                    holder.value = QueryAnnotationUtil.filterNullValue(holder.value, true);
                }

                //JPA QL 是默认支持 in 语法，为了兼容SQL，拆成多个问号
                int eleCount = QueryAnnotationUtil.eleCount(holder.value);

                if (eleCount < 1) {
                    return "";
                }

                if (eleCount > 1) {
                    return prefix + aroundColumnPrefix(name) + " Between " + genParamExpr(" " + between.op() + " ", getParamPlaceholder(), eleCount) + suffix;
                } else {
                    return prefix + aroundColumnPrefix(name) + " >= " + getParamPlaceholder() + suffix;
                }

            } else if (opAnno instanceof Where) {

                Where whereAnno = (Where) opAnno;


                String expr = doReplace(op + " " + autoAroundParentheses(prefix, whereAnno.statement(), suffix), whereAnno.useVarValue(), holder);


                return expr;

            } else {

                String subQuery = getSubQuery(opAnno);

                String expr = "";

                //最优先使用自定义的子查询
                if (StringUtils.hasText(subQuery)) {

                    expr = autoAroundParentheses("", doReplace(subQuery, false, holder), "");

                } else if (complexType) {

                    expr = autoAroundParentheses("", buildSubQuery(holder), "");

                } else {
                    expr = getParamPlaceholder();
                }

                return aroundColumnPrefix(name) + " " + op + " " + prefix + " " + expr + " " + suffix;
            }
        } catch (Exception e) {
            if (e instanceof StatementBuildException)
                throw (StatementBuildException) e;
            else
                throw new StatementBuildException(name + " " + opAnno + " 构建语句失败", e);
        }
    }

    //匹配样式：${:paramName}
    private static final Pattern namedParamPattern = Pattern.compile("(\\$\\{\\s*:\\s*([\\w._]+)\\s*\\})");


    /**
     * 替换所有的变量
     *
     * @param ql
     * @param useVarValue
     * @param holder
     * @return
     */
    public String doReplace(String ql, boolean useVarValue, ValueHolder holder) {

        if (!StringUtils.hasText(ql)) {
            return ql;
        }

        Matcher matcher = namedParamPattern.matcher(ql);


        Map<String, Object> params = QueryAnnotationUtil.copyMap(true, null,
                getDaoContextValues(),
                ObjectUtil.copyField2Map(holder.root, null),
                ObjectUtil.copyField2Map(holder.value, null));


        List paramValues = new ArrayList(7);

        while (matcher.find()) {

            String paramName = matcher.group(2);

            if (!params.containsKey(paramName))
                throw new StatementBuildException("[" + ql + "] param [" + paramName + "] no exist in current env context:" + params);

            try {
                paramValues.add(ObjectUtil.getIndexValue(params, paramName));
            } catch (Exception e) {
                throw new StatementBuildException("<<<" + ql + ">> can't ge param " + paramName, e);
            }

        }

        //替换成 ？ 号参数
        ql = matcher.replaceAll(getParamPlaceholder());

        //
        if (paramValues.size() > 0) {
            holder.value = paramValues;
        } else if (!useVarValue) {
            //如果没有发现参数，然后又要求不使用变量值
            //忽略参数值
            holder.value = EMPTY_PARAM_VALUES;
        }

        return ql;

    }


    /**
     * 根据参数个数生成问号
     *
     * @param n
     * @return
     */
    static String genParamExpr(String delimiter, String txt, int n) {

        if (n < 1) return "";

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < n; i++) {

            if (i > 0)
                buf.append(delimiter);

            buf.append(txt);
        }

        return buf.toString();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean hasContent(String text) {
        return text != null && text.trim().length() > 0;
    }

    protected String getText(String text, String prefix, String suffix, String defaultV) {
        return hasContent(text) ? prefix + text + suffix : defaultV;
    }

    protected String getText(String text, String defaultV) {
        return hasContent(text) ? text : defaultV;
    }

    protected String genWhereStatement() {

        return getText(whereExprRootNode.toString(), " Where ", " ", " ");

    }

    protected String getParamPlaceholder() {
//        return localParamPlaceholder != null ? localParamPlaceholder : getDao().getParamPlaceholder(isNative());
        return getDao().getParamPlaceholder(isNative());
    }

    protected abstract MiniDao getDao();

    /**
     * @return
     */
    protected boolean isNative() {
        return this.nativeQL;
    }

    protected String genEntityStatement() {

        if (StringUtils.hasText(this.tableName))
            return tableName + " " + getText(alias, " ");
        else if (entityClass != null)
            return entityClass.getName() + " " + getText(alias, " ");

        throw new IllegalArgumentException("entityClass or tableName is null");
    }

    protected String aroundColumnPrefix(String column) {

        //如果包含占位符，则直接返回
        if (column.contains(getParamPlaceholder().trim())) {
            return column;
        }

        // :?P

        String prefix = getText(alias, "", ".", "");

        return column.trim().startsWith(prefix) ? column : prefix + column;
    }

    protected String genFromStatement() {
        return " From " + genEntityStatement();
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
        if (anno == null)
            return true;

        boolean require = false;

        try {
            require = (boolean) anno.annotationType().getDeclaredMethod("require").invoke(anno);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        String conditionExpr = null;

        try {
            conditionExpr = (String) anno.annotationType().getDeclaredMethod("condition").invoke(anno);
        } catch (NoSuchMethodException e) {
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //如果没有内容默认为true
        if (!StringUtils.hasText(conditionExpr)) {
            return true;
        }

        boolean isOK = evalExpr(root, value, conditionExpr);

        //如果是必须的，但条件又不成立，则抛出异常
        if (require && !isOK) {
            throw new IllegalArgumentException(String.format("field [%s] is require, annotation [%s] condition[%s] must be true"
                    , name, anno.annotationType().getSimpleName(), conditionExpr));
        }

        return isOK;
    }


    protected <T> T evalExpr(Object root, Object value, String expr) {

        Map<String, Object> ctx = new HashMap<>();

//        ctx.put("_this", root);
        ctx.put("_val", value);

        ctx.put("_isSelect", (this instanceof SelectDao));
        ctx.put("_isUpdate", (this instanceof UpdateDao));
        ctx.put("_isDelete", (this instanceof DeleteDao));


        return QueryAnnotationUtil.evalSpEL(root, expr, DaoContext.getGlobalContext(), DaoContext.getThreadContext(), this.context, ctx);
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

    protected void endLogic() {
        whereExprRootNode.endGroup();
    }

    private void endLogicGroup(Object bean, Annotation logicAnnotation, Object value) {
        //如果遇到逻辑结束
        if (logicAnnotation instanceof END) {
            end();
        }
    }

    /**
     * 尝试转换值
     *
     * @param name
     * @param value
     * @return
     */
    protected Object tryToConvertValue(String name, Object value) {

        if (value == null || entityClass == null) {
            return value;
        }

        //期望的类型
        Class<?> expectType = QueryAnnotationUtil.getFieldType(entityClass, name);

        //如果类型为null
        if (expectType == null || expectType.isInstance(value)) {
            return value;
        }

        //尝试转换
        //如果是数组，将不尝试转换
        if (!value.getClass().isArray() && !(value instanceof Iterable)) {
            try {
                value = ObjectUtil.convert(value, expectType);
            } catch (Exception e) {
                logger.warn("convert attr " + name + " error", e);
            }
        }

        //@todo 尝试转换数组或是集合

        return value;
    }

}
