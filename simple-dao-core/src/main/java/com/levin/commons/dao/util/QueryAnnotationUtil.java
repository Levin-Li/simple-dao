package com.levin.commons.dao.util;


import com.levin.commons.dao.DaoContext;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.order.SimpleOrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.Locker;
import com.levin.commons.service.support.VariableInjector;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.containsWhitespace;
import static org.springframework.util.StringUtils.hasText;


/**
 *
 */
public abstract class QueryAnnotationUtil {

    private final static Logger logger = LoggerFactory.getLogger(QueryAnnotationUtil.class);


    public static final String ANNOTATION_VALUE_KEY = "value";


    @Eq
    @Gt
    @Gte
    @In
    @Between
    @Like
    @Contains
    @StartsWith
    @EndsWith
    @Lt
    @Lte
    @Where
/////////////////////////////////
    @NotEq
    @NotIn
    @NotLike
    @IsNotNull
    @IsNull

    @Exists
    @NotExists

    @Ignore

    @PrimitiveValue

    //逻辑注解
    @AND
    @OR
    @END

    //统计注解
    @GroupBy
    @Avg
    @Count
    @Max
    @Min
    @Sum
    //////////////////////////////////

    @OrderBy
    @SimpleOrderBy
    /////////////////////////////////
    @Select
//    @SelectColumn

    @Update
//    @UpdateColumn

    private static final Map<String, Annotation> allInstanceMap;

    //条件表达式

    //缓存属性
    private static final Map<String, List<Field>> cacheFields = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> hasSelectAnnotationCache = new ConcurrentHashMap<>();

    public static final Map<String, String> entityTableNameCaches = new ConcurrentHashMap<>();

    public static final Map<String, Class<?>> tableNameMappingEntityClassCaches = new ConcurrentHashMap<>();

    /**
     * 实体类字段名和数据库字段名映射关系
     */
    protected static final Map<String/* 类名 */, Map<String/* 类字段名 */, String /* 数据库列名 */>> entityFieldNameMap = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, String>> propertyNameMapCaches = new ConcurrentHashMap<>();

    /**
     * 实体对象，可空字段缓存
     */
    protected static final Map<String, Boolean> entityClassNullableFields = new ConcurrentHashMap<>();


    private static final Map<String, String[]> entityInjectAttrNames = new ConcurrentHashMap<>();


    static {

        Map<String, Annotation> temp = new HashMap<>(64);

        try {
            Annotation[] annotations = QueryAnnotationUtil.class.getDeclaredField("allInstanceMap").getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                temp.put(annotation.annotationType().getSimpleName(), annotation);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        //不可修改的
        allInstanceMap = Collections.unmodifiableMap(temp);
    }

    /**
     * 获取所有注解
     *
     * @return
     */
    public static Map<String, Annotation> getAllAnnotations() {
        return allInstanceMap;
    }


    /**
     * 获取目标类的注入域
     *
     * @param entityClass
     * @return
     */
    public static String[] getDaoInjectAttrs(final Class<?> entityClass) {

        final VariableInjector variableInjector = DaoContext.getVariableInjector();

        final String injectDomain = variableInjector.getInjectDomain();

        String[] injectAttrs = entityInjectAttrNames.computeIfAbsent(entityClass.getName() + ":" + injectDomain, (key) -> {

            ArrayList attrs = new ArrayList(7);

            //找出 InjectVar的字段，并且是 injectDomain 相同的注入域
            ReflectionUtils.doWithFields(entityClass, field -> {
                        attrs.add(field.getName());
                    }
                    , field -> field.isAnnotationPresent(InjectVar.class) && variableInjector.isDomainMatch(field)
            );

            return (String[]) attrs.toArray(new String[attrs.size()]);

        });

        return injectAttrs;
    }


    /**
     * 合并数组
     *
     * @param addArray           增加的数组
     * @param defaultReturnArray 默认返回的数组
     * @return
     */
    public static String[] mergeArray(String[] addArray, String[] defaultReturnArray) {

        if (addArray != null
                && addArray.length > 0) {
            if (defaultReturnArray == null || defaultReturnArray.length == 0) {
                defaultReturnArray = addArray;
            } else {

                String[] temp = new String[addArray.length + defaultReturnArray.length];

                System.arraycopy(defaultReturnArray, 0, temp, 0, defaultReturnArray.length);
                System.arraycopy(addArray, 0, temp, defaultReturnArray.length, addArray.length);

                defaultReturnArray = temp;
            }
        }

        return defaultReturnArray;
    }

    /**
     * 是否不允许空
     *
     * @param propertyName
     * @return
     */
    public static boolean isNullable(Class<?> entityClass, String propertyName) {

        return entityClassNullableFields.computeIfAbsent(entityClass.getName() + "." + propertyName
                , key -> {

                    Field field = ReflectionUtils.findField(entityClass, propertyName);

                    if (field == null) {
                        throw new RuntimeException(new NoSuchFieldException(key));
                    }

                    Column column = field.getAnnotation(Column.class);

                    return column == null || column.nullable();

                });

    }

    /**
     * @param entityClasses
     * @param nameConvert
     */
    public static void addEntityClassMapping(Collection<Class<?>> entityClasses, Function<Class<?>, String> nameConvert) {

        Optional.ofNullable(entityClasses).ifPresent(entityClassList -> {

            entityClassList.stream().filter(Objects::nonNull).forEach(cls -> {

                tableNameMappingEntityClassCaches.put(cls.getName(), cls);

                String name = nameConvert.apply(cls);

                if (StringUtils.hasText(name)) {
                    tableNameMappingEntityClassCaches.put(name, cls);
                }

            });
        });
    }

    /**
     * 获取实体类类字段名对应的表列名
     *
     * @param entityClass
     * @param fieldName
     * @return
     */
    public static String getEntityColumnName(Class<?> entityClass, String fieldName, Function<String, String> columnNameConvert) {

        if (!ExprUtils.isValidClass(entityClass)
                || !hasText(fieldName) || containsWhitespace(fieldName.trim())) {
            return fieldName;
        }

        //
        Map<String, String> fieldMap = entityFieldNameMap.computeIfAbsent(entityClass.getName(), key -> {

            Map<String, String> entityMap = new ConcurrentHashMap<>();

            ReflectionUtils.doWithFields(entityClass, field -> {

                if (Modifier.isStatic(field.getModifiers())) {
                    return;
                }

                String column = Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .filter(StringUtils::hasText)
                        .orElse(
                                Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                                        .map(JoinColumn::name)
                                        .filter(StringUtils::hasText)
                                        //默认使用转换值
                                        .orElse(columnNameConvert.apply(field.getName()))
                        );

                if (hasText(column)) {
                    entityMap.put(field.getName(), column);
                }
            });

            return Collections.unmodifiableMap(entityMap);

        });


        return fieldMap.getOrDefault(fieldName, fieldName);
    }


    public static Class<?> getEntityClassByTableName(String tableName) {
        return tableNameMappingEntityClassCaches.get(tableName);
    }


    /**
     * 获取表名
     *
     * @param entityClassName
     * @return
     */
    @SneakyThrows
    public static String getTableNameByEntityClassName(String entityClassName, Function<String, String> tableNameConvert) {

        String name = entityTableNameCaches.get(entityClassName);

        if (hasText(name)) {
            return name;
        }

        //通过类加载
        return getTableNameByAnnotation(Thread.currentThread().getContextClassLoader().loadClass(entityClassName), tableNameConvert);
    }

    /**
     * 获取表名
     *
     * @param entityClass
     * @return
     */
    public static String getTableNameByAnnotation(Class<?> entityClass, Function<String, String> tableNameConvert) {

        if (entityClass == null) {
            return null;
        }

        return entityTableNameCaches.computeIfAbsent(entityClass.getName(), key ->

                Optional.ofNullable(entityClass.getAnnotation(Table.class))
                        .filter((t) -> hasText(t.name()))
                        .map(Table::name)
                        .orElse(
                                //转换名称
                                tableNameConvert.apply(
                                        //否则取实体名
                                        Optional.ofNullable(entityClass.getAnnotation(Entity.class))
                                                .filter(t -> hasText(t.name()))
                                                .map(Entity::name).orElse(
                                                        //否则取类名
                                                        entityClass.getSimpleName()
                                                )
                                )
                        )
        );

    }

    /**
     * 是否时同个包的
     *
     * @param annotation
     * @param type
     * @return
     */
    public static boolean isSamePackage(Annotation annotation, Class<? extends Annotation> type) {
        return annotation != null && annotation.annotationType().getPackage().getName().equals(type.getPackage().getName());
    }

    /**
     * 递归抚平
     *
     * @param valueList
     * @param paramValues
     * @return
     */
    public static List flattenParams(List valueList, Object... paramValues) {

        if (valueList == null) {
            valueList = new LinkedList();
        }

        if (paramValues == null) {
            return valueList;
        }

        for (Object paramValue : paramValues) {
            if (paramValue instanceof Iterable) {
                for (Object pv : ((Iterable) paramValue)) {
                    flattenParams(valueList, pv);
                }
            } else if (paramValue != null && paramValue.getClass().isArray()) {
                int length = Array.getLength(paramValue);
                for (int i = 0; i < length; i++) {
                    flattenParams(valueList, Array.get(paramValue, i));
                }
            } else if (paramValue instanceof Map) {
                valueList.add(paramValue);
            } else {
                valueList.add(paramValue);
            }
        }

        return valueList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 过滤出指定包的注解
     * <p>
     *
     * @param packageName  精确相等的包名，不包含子包
     * @param annotations
     * @param excludeTypes
     * @return
     */
    public static List<Annotation> getAnnotationsByPackage(String packageName, Annotation[] annotations, Class<? extends Annotation>... excludeTypes) {

        List<Annotation> result = new ArrayList<>(3);

        if (annotations == null) {
            return result;
        }

        if (excludeTypes == null) {
            excludeTypes = new Class[0];
        }

        for (Annotation anno : annotations) {

            //相同的包括名
            if (!anno.annotationType().getPackage().getName().equals(packageName)) {
                continue;
            }

            for (Class<? extends Annotation> excludeType : excludeTypes) {
                if (excludeType.isInstance(anno)) {
                    anno = null;
                    break;
                }
            }

            if (anno != null) {
                result.add(anno);
            }
        }

        return result;
    }

    /**
     * 获一个逻辑注解，如果有多个将生产异常
     *
     * @param varName
     * @param varAnnotations
     * @return
     */
    public static List<Annotation> getLogicAnnotation(String varName, Annotation[] varAnnotations) {

        List<Annotation> annotations = getAnnotationsByPackage(AND.class.getPackage().getName(), varAnnotations, END.class);

        if (annotations.size() > 1) {
            //  throw new StatementBuildException(varName + " --> 同一个位置只允许定义一个逻辑注解，已经定义的注解：" + annotations);
        }

        return annotations;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Class<?> getFieldType(Class<?> type, String propertyName) {
        return getFieldType(type, propertyName, null);
    }

    /**
     * 获取一个对象属性的数据类型
     * <p/>
     * 目前不支持泛型
     *
     * @param type
     * @param propertyName 支持复杂属性
     * @return
     */
    public static Class<?> getFieldType(Class<?> type, String propertyName, BiFunction<Field, Class<?>, Class<?>> biFunction) {

        if (type == null || !hasText(propertyName)) {
            return null;
        }

        String[] names = propertyName.split("\\.");

        ResolvableType owner = null;

        Field field = null;

        for (int i = 0; i < names.length; i++) {

            field = ReflectionUtils.findField(type, names[i]);

            if (field == null) {
                return null;
            }

            owner = ResolvableType.forField(field, owner);

            type = owner.resolve(field.getType());

        }

        return biFunction != null ? biFunction.apply(field, type) : type;
    }


    public static <T extends Annotation> T getAnnotation(Class<T> type) {
        return (T) allInstanceMap.get(type.getSimpleName());
    }

    public static <T extends Annotation> T getAnnotation(String simpleName) {
        return (T) allInstanceMap.get(simpleName);
    }

    public static Annotation[] getAnnotations(Class<? extends Annotation>... types) {

        if (types == null || types.length < 1) {
            return new Annotation[0];
        }

        Annotation[] result = new Annotation[types.length];

        int i = 0;

        for (Class<? extends Annotation> type : types) {
            Annotation annotation = allInstanceMap.get(type.getSimpleName());

            if (annotation == null) {
                throw new StatementBuildException("Annotation " + type.getName() + " instance cache not found");
            }

            result[i++] = annotation;
        }


        return result;
    }


    public static Op getOp(Annotation opAnno) {

        Op op = null;

        try {
            op = (Op) ReflectionUtils.findMethod(opAnno.annotationType(), E_C.op).invoke(opAnno);
        } catch (Exception e) {

        }

        if (op == null) {
            op = Stream.of(Op.values())
                    .filter(o -> o.name().contentEquals(opAnno.annotationType().getSimpleName()))
                    .findFirst()
                    .orElse(null);
        }

        return op;
    }

    /**
     * 获取属性名称
     *
     * @param opAnno
     * @param name
     * @return
     */
    public static String tryGetJpaEntityFieldName(Annotation opAnno, Class<?> entityClass, @NotNull String name) {

        if (opAnno == null) {
            return name;
        }

        //提升字段查找性能

        String newName = null;

        try {
            newName = (String) ReflectionUtils.findMethod(opAnno.annotationType(), ANNOTATION_VALUE_KEY).invoke(opAnno);
        } catch (Exception e) {
//            ReflectionUtils.rethrowRuntimeException(e);
        }

        if (hasText(newName)) {
            return newName;
        }

        if (entityClass == null) {
            return name;
        }

        //第二级缓存
        Map<String, String> entityAttrMap =
                propertyNameMapCaches.computeIfAbsent(entityClass.getName(), key -> new ConcurrentHashMap<>());

        newName = entityAttrMap.computeIfAbsent(opAnno.annotationType().getName() + "-" + name, key -> {

            String findName = null;

            if (ReflectionUtils.findField(entityClass, name) == null) {
                // 以下逻辑是自动去除查找，去除注解名称以后的属性
                int len = opAnno.annotationType().getSimpleName().length();

                if (name.length() > len) {
                    findName = Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
                    if (ReflectionUtils.findField(entityClass, findName) == null) {
                        findName = null;
                    }
                }
            }

            //重要逻辑点，返回空字符串避免反复从Map查找
            return findName != null ? findName : "";

        });

        return (hasText(newName)) ? newName : name;

    }

    /**
     * 获取第一个匹配注解
     *
     * @param annotations
     * @param types
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A findFirstMatched(Annotation[] annotations, Class<? extends Annotation>... types) {

        if (annotations == null || types == null) {
            return null;
        }

        return (A) Stream.of(annotations)
                .filter(Objects::nonNull)
                .filter(a -> Stream.of(types).anyMatch(t -> t == a.annotationType()))
                .findFirst()
                .orElse(null);

//        for (Annotation annotation : annotations) {
//
//            if (annotation == null) {
//                continue;
//            }
//
//            for (Class type : types) {
//
//                if (type == null) {
//                    continue;
//                }
//
//                if (annotation.annotationType() == type) {
//                    return (A) annotation;
//                }
//            }
//        }
//
//        return null;

    }


    /**
     * 展开嵌套对象
     *
     * @param resultList
     * @param queryObjs
     * @return
     */
    public static List<Object> expandAndFilterNull(List<Object> resultList, Iterable<Object> queryObjs) {

        if (resultList == null) {
            resultList = new ArrayList<>();
        }

        if (queryObjs == null) {
            return resultList;
        }

        for (Object queryObj : queryObjs) {

            if (queryObj == null) {
                //忽略空值
                continue;
            }
            //如果是类
            if (queryObj instanceof Class) {
                resultList.add(queryObj);
            } else if (queryObj.getClass().isArray()) {
                //数组
                expandAndFilterNull(resultList, Arrays.asList((Object[]) queryObj));
            } else if (queryObj instanceof Iterable) {
                //可迭代对象
                expandAndFilterNull(resultList, ((Iterable) queryObj));
            } else {
                resultList.add(queryObj);
            }

        }

        return resultList;
    }


    /**
     * 过滤调查询对象不需要的类型
     *
     * @param queryObjList
     * @return
     */
    public static List<Object> filterQueryObjSimpleType(List<?> queryObjList, Predicate excludePredicate) {

        if (queryObjList == null || queryObjList.isEmpty()) {
            return Collections.emptyList();
        }

        //过滤不需要的类
        return queryObjList.stream()
                .filter(Objects::nonNull)
                .filter(o -> {
                            Class<?> c = (o instanceof Class) ? (Class<?>) o : o.getClass();
                            //不是简单的类型
                            return !BeanUtils.isSimpleProperty(c)
                                    && !c.isAnnotation()
                                    && !c.isEnum()
                                    && !QueryAnnotationUtil.isRootObjectType(c)
                                    && !QueryAnnotationUtil.isIgnore(c);
                        }
                )
                .filter(o -> excludePredicate == null || !excludePredicate.test(o))
                .collect(Collectors.toList());
    }

    /**
     * @param o
     * @return
     */
    public static boolean isJpaEntityClass(Object o) {
        return o instanceof Class
                && (((Class<?>) o).isAnnotationPresent(Entity.class) || ((Class<?>) o).isAnnotationPresent(MappedSuperclass.class));
    }

    /**
     * 获取查询的目标对象，同时清除
     *
     * @param queryObjList
     * @param onEntityClassConsumer
     * @return
     */
    public static List<Object> tryGetEntityClassAndClear(List<Object> queryObjList, Consumer<Class<?>> onEntityClassConsumer) {

        if (queryObjList == null || queryObjList.isEmpty()) {
            return Collections.emptyList();
        }

        //如果主体类还没有定义，则尝试设置实体类，优先级低于 @TargetOption，会被覆盖
        if (onEntityClassConsumer != null) {

            Class<?> type = (Class<?>) queryObjList.stream()
                    .filter(QueryAnnotationUtil::isJpaEntityClass)
                    .findFirst()
                    .orElse(null);

            if (type != null) {
                onEntityClassConsumer.accept(type);
            }
        }

        //清除实体类
        return queryObjList.stream()
                .filter(Objects::nonNull)
                //剔除jpa 实体类
                .filter(o -> !isJpaEntityClass(o))
                .collect(Collectors.toList());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <K, V> Map<K, V> copyMap(boolean isOnlyCopyPrimitive, Map<K, V> output, Map<K, V>... sources) {

        if (output == null) {
            output = new LinkedHashMap();
        }

        for (Map<K, V> source : sources) {

            if (source == null) {
                continue;
            }

            for (Map.Entry<K, V> entry : source.entrySet()) {

                if (!isOnlyCopyPrimitive
                        || entry.getValue() == null
                        || isPrimitive(entry.getValue().getClass())) {

                    output.put(entry.getKey(), entry.getValue());

                }
            }
        }

        return output;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean hasSelectAnnotation(Annotation... annotations) {

        for (Annotation annotation : annotations) {
            if (isSamePackage(annotation, Select.class)
                    || isSamePackage(annotation, GroupBy.class)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasSelectStatementField(Class type) {
        return hasSelectStatementField(type, null);
    }

    /**
     * 是否有选择注解的字段
     * <p>
     * 不支持嵌套
     *
     * @param type
     * @return
     */
    public static boolean hasSelectStatementField(Class<?> type, ResolvableType resolvableType) {

        if (type == null) {
            return false;
        }

        Boolean hasAnno = hasSelectAnnotationCache.get(type.getName());

        if (hasAnno == null) {

            hasAnno = hasSelectAnnotation(type.getAnnotations());

            if (hasAnno) {
                hasSelectAnnotationCache.put(type.getName(), hasAnno);
                return hasAnno;
            }

            if (resolvableType == null) {
                resolvableType = ResolvableType.forClass(type);
            }

            List<Field> cacheFields = getNonStaticFields(type);

            for (Field field : cacheFields) {

                //如果是统计或是选择注解
                if (hasSelectAnnotation(field.getAnnotations())) {
                    hasAnno = true;
                    break;
                }
                //如果示复杂对象
                ResolvableType forField = ResolvableType.forField(field, resolvableType);

                Class<?> fieldType = forField.resolve(field.getType());

                //防止递归
                if (fieldType != type && isComplexType(fieldType, null)) {
                    hasAnno = hasSelectStatementField(fieldType, forField);
                }

                if (hasAnno) {
                    break;
                }

            }

            hasSelectAnnotationCache.put(type.getName(), hasAnno);
        }

        return hasAnno;
    }

    public static List<Field> getNonStaticFields(Class<?> type) {
        return getFieldsFromCache(type).stream()
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
                .collect(Collectors.toList());
    }

    /**
     * 获取字段列表，以包括所有父对象的子段
     * <p>
     * 不包括
     *
     * @param type
     * @return
     */
    public static List<Field> getFieldsFromCache(Class<?> type) {
        return cacheFields.computeIfAbsent(type.getName(), key -> getAllFields(type));
    }


    /**
     * 递归获取所有的字段，包括父类的字段
     *
     * @param type
     * @return
     */
    public static List<Field> getAllFields(Class<?> type) {

        if (type == null
                || isRootObjectType(type)
                || isPrimitive(type)
                || isArray(type)
                || isIgnore(type)) {
            return Collections.emptyList();
        }

        List<Field> fields = new ArrayList<>(16);

        ReflectionUtils.doWithFields(type, fields::add);

        Collections.reverse(fields);

        return Collections.unmodifiableList(fields);
    }


    /**
     * 获取集合大小
     * <p/>
     * 如果不是集合，则返回1
     *
     * @param value
     * @return
     */
    public static int eleCount(Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Collection) {
            return ((Collection) value).size();
        }

        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }

        return 1;
    }


    /**
     * 判定复杂对象的方法
     *
     *
     * <p>
     * 如果是数组，Map , Iterable 等都不认为是复杂对象
     *
     * <p>
     * 关键方法
     * <p>
     * 如果是数组，必须要求不存在原子元素，并且不为空数组，并且元素不都是Null
     *
     * @param varType
     * @param value
     * @return
     */
    public static boolean isComplexType(Class<?> varType, Object value) {

        if (varType == null && value != null) {

            // 是数组并且有原子元素
            if (QueryAnnotationUtil.isArrayAndExistPrimitiveElement(value)) {
                return false;
            }

            varType = value.getClass();
        }

        return varType != null

                //不是数组
                && !varType.isArray()

                && !QueryAnnotationUtil.isPrimitive(varType)

                //不是 Object.class
                && !varType.getName().equals(Object.class.getName())

//                && !Object[].class.isAssignableFrom(varType)
                && !Map.class.isAssignableFrom(varType) //并且不是 Map

                //不是可迭代
                && !Iterable.class.isAssignableFrom(varType) //并且不是可迭代对象

                //不是特殊标记的
                && !varType.isAnnotationPresent(PrimitiveValue.class);
    }


    /**
     * 如果value是集合，则移除集合中的null对象，并返回新的集合对象
     *
     * @param value
     * @return
     */
    public static Object filterNullValue(Object value, boolean isFilterEmptyString) {

        ////@todo 优化性能

        if (isNull(value, isFilterEmptyString)) {
            return null;
        }

        if (value.getClass().isArray()) {

            int length = Array.getLength(value);

            List list = new ArrayList(length);

            for (int i = 0; i < length; i++) {
                Object v = Array.get(value, i);
                if (!isNull(v, isFilterEmptyString)) {
                    list.add(v);
                }
            }

            return list;

        } else if (value instanceof Iterable) {

            List list = new ArrayList();

            for (Object v : (Iterable) value) {
                if (!isNull(v, isFilterEmptyString)) {
                    list.add(v);
                }
            }

            return list;

        }

        return value;
    }


    private static boolean isNull(Object value, boolean isFilterEmptyString) {
        return value == null || (isFilterEmptyString && value instanceof CharSequence && !hasText((CharSequence) value));
    }


    public static boolean isArray(Type type) {
        return (type instanceof GenericArrayType)
                || ((type instanceof Class) && ((Class) type).isArray());
    }

    public static boolean isIterable(Type type) {
        return (type instanceof Class)
                && Iterable.class.isAssignableFrom(((Class) type));
    }

    public static boolean isMap(Type type) {
        return (type instanceof Class)
                && Map.class.isAssignableFrom(((Class) type));
    }

    public static boolean isRootObjectType(Type type) {
        return (type instanceof Class)
                && (Object.class == type || Object.class.getName().equals(type.getTypeName()));
    }

    /**
     * 是数组并且有原子元素
     *
     * @param value
     * @return
     */
    public static boolean isArrayAndExistPrimitiveElement(Object value) {

        boolean isArray = (value != null) && value.getClass().isArray();

        if (!isArray) {
            return false;
        }

        Class<?> componentType = value.getClass().getComponentType();
        if (isPrimitive(componentType)) {
            return true;
        }

        int length = Array.getLength(value);

        while (--length >= 0) {
            Object ele = Array.get(value, length);
            if (ele != null && isPrimitive(ele.getClass())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 复杂类型的判定条件为：对象，对象数组（数组元素为非原子对象）
     * <p>
     * <p>
     * 注意：
     * 原子类型包括 Date和枚举
     *
     * @param type
     * @return
     */
    public static boolean isPrimitive(Class<?> type) {
        return BeanUtils.isSimpleProperty(type);
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz.isAnnotationPresent(Ignore.class);
    }

    public static boolean isIgnore(Field field) {
        return field.isAnnotationPresent(Ignore.class);
    }

}
