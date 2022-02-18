package com.levin.commons.dao.util;


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
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.service.support.Locker;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.containsWhitespace;
import static org.springframework.util.StringUtils.hasText;

//import com.levin.commons.dao.annotation.select.SelectColumn;
//import com.levin.commons.dao.annotation.update.UpdateColumn;

/**
 *
 */
public abstract class QueryAnnotationUtil {

    private final static Logger logger = LoggerFactory.getLogger(QueryAnnotationUtil.class);


    public static final String ANNOTITION_VALUE = "value";


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
    private static final Map<String, List<Field>> cacheFields = new ConcurrentReferenceHashMap<>();


    private static final Map<String, Boolean> hasSelectAnnotationCache = new ConcurrentReferenceHashMap<>();


    public static final Map<String, Object> cacheEntityOptionMap = new ConcurrentReferenceHashMap<>();
    private static final Locker cacheEntityOptionMapLocker = Locker.build();

    public static final Map<String, String> entityTableNameCaches = new ConcurrentReferenceHashMap<>();
    public static final Map<String, Class<?>> tableNameMappingEntityClassCaches = new ConcurrentReferenceHashMap<>();

    /**
     * 实体类字段名和数据库字段名映射关系
     */
    protected static final Map<String/* 类名 */, Map<String/* 类字段名 */, String /* 数据库列名 */>> entityFieldNameMap = new ConcurrentReferenceHashMap<>();

    private static final Locker entityFieldNameMapLocker = Locker.build();

    private static final ContextHolder<String, String> propertyNameMapCaches = ContextHolder.buildContext(false);

    /**
     * 实体对象，可空字段缓存
     */
    protected static final Map<String, Boolean> entityClassNullableFields = new ConcurrentReferenceHashMap<>();


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
     * 是否不允许空
     *
     * @param propertyName
     * @return
     */
    public static boolean isNullable(Class entityClass, String propertyName) {

        String key = entityClass.getName() + "." + propertyName;

        Boolean aBoolean = entityClassNullableFields.get(key);

        if (aBoolean == null) {

            Field field = ReflectionUtils.findField(entityClass, propertyName);

            if (field == null) {
                throw new RuntimeException(new NoSuchFieldException(key));
            }

            Column column = field.getAnnotation(Column.class);

            aBoolean = column == null || column.nullable();

            entityClassNullableFields.put(key, aBoolean);
        }

        return aBoolean;
    }


    /**
     * 获取实体类类字段名对应的表列名
     *
     * @param entityClass
     * @param fieldName
     * @return
     */
    public static String getColumnName(Class entityClass, String fieldName) {

        if (!ExprUtils.isValidClass(entityClass)
                || !hasText(fieldName) || containsWhitespace(fieldName.trim())) {
            return fieldName;
        }

//        if (!entityClass.isAnnotationPresent(Entity.class)) {
//            return fieldName;
//        }

        String key = entityClass.getName();

        Map<String, String> fieldMap = entityFieldNameMap.get(key);

        if (fieldMap == null) {

            synchronized (entityFieldNameMapLocker.getLock(key)) {

                fieldMap = entityFieldNameMap.get(key);

                //等到锁后如果还是为空
                if (fieldMap == null) {

                    fieldMap = new HashMap<>(7);

                    final Map tempMap = fieldMap;

                    ReflectionUtils.doWithFields(entityClass, field -> {

                        String column = Optional.ofNullable(field.getAnnotation(Column.class))
                                .map(Column::name)
                                .filter(StringUtils::hasText)
                                .orElse(
                                        Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                                                .map(JoinColumn::name)
                                                .filter(StringUtils::hasText)
                                                .orElse(null)
                                );

                        if (hasText(column)) {
                            tempMap.put(field.getName(), column);
                        }
                    });

                    entityFieldNameMap.put(key, Collections.unmodifiableMap(tempMap));
                }

            }
        }

        return fieldMap.getOrDefault(fieldName, fieldName);
    }


    public static Class<?> getEntityClassByTableName(String tableName) {
        return tableNameMappingEntityClassCaches.get(tableName);
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
     * 获取表名
     *
     * @param entityClassName
     * @return
     */
    @SneakyThrows
    public static String getTableNameByEntityClassName(String entityClassName) {

        String name = entityTableNameCaches.get(entityClassName);

        if (hasText(name)) {
            return name;
        }

        //通过类加载
        return getTableNameByAnnotation(Thread.currentThread().getContextClassLoader().loadClass(entityClassName));
    }

    /**
     * 获取表名
     *
     * @param entityClass
     * @return
     */
    public static String getTableNameByAnnotation(Class<?> entityClass) {

        if (entityClass == null) {
            return null;
        }

        String name = entityTableNameCaches.get(entityClass.getName());

        if (hasText(name)) {
            return name;
        }

        name = Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .filter((t) -> hasText(t.name()))
                .map(Table::name)
                .orElse(
                        //否则取实体名
                        Optional.ofNullable(entityClass.getAnnotation(Entity.class))
                                .filter(t -> hasText(t.name()))
                                .map(Entity::name).orElse(
                                //否则取类名
                                entityClass.getSimpleName()
                        )
                );

        entityTableNameCaches.put(entityClass.getName(), name);

        return name;
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
     * 获取缓存
     *
     * @return
     */
    public static EntityOption getEntityOption(Class entityClass) {

        if (entityClass == null) {
            return null;
        }

        String className = entityClass.getName();

        Object value = cacheEntityOptionMap.get(className);

        synchronized (cacheEntityOptionMapLocker.getLock(className)) {
            if (value == null) {
                value = entityClass.getAnnotation(EntityOption.class);
                cacheEntityOptionMap.put(className, value != null ? value : Boolean.FALSE);
            }
        }

        return (value instanceof EntityOption) ? (EntityOption) value : null;
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     *
     * @param packageName
     * @param annotations
     * @param excludeTypes
     * @return
     */
    public static List<Annotation> getAnnotationsByPackage(String packageName, Annotation[] annotations, Class<? extends Annotation>... excludeTypes) {

        List<Annotation> result = new ArrayList<>(2);

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
    public static String tryGetJpaEntityFieldName(Annotation opAnno, Class entityClass, @NotNull String name) {

        if (opAnno == null) {
            return name;
        }

        //提升字段查找性能

        String newName = null;

        try {
            newName = (String) ReflectionUtils.findMethod(opAnno.annotationType(), ANNOTITION_VALUE).invoke(opAnno);
        } catch (Exception e) {
//            ReflectionUtils.rethrowRuntimeException(e);
        }

        if (hasText(newName)) {
            return newName;
        }

        if (entityClass == null) {
            return name;
        }

        String key = entityClass.getName() + "-" + opAnno.annotationType().getSimpleName() + "-" + name;

        newName = propertyNameMapCaches.getAndAutoPut(key, v -> true, () -> {

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

            return findName;

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
    public static boolean hasSelectStatementField(Class type, ResolvableType resolvableType) {

        if (type == null) {
            return false;
        }

        Boolean hasAnno = hasSelectAnnotationCache.get(type.getName());

        if (hasAnno == null) {

            hasAnno = false;

            if (resolvableType == null) {
                resolvableType = ResolvableType.forClass(type);
            }

            List<Field> cacheFields = getNonStaticFields(type);

            for (Field field : cacheFields) {
                //如果是统计或是选择注解
                if (
                        field.isAnnotationPresent(Select.class)
                                || field.isAnnotationPresent(Avg.class)
                                || field.isAnnotationPresent(Count.class)
                                || field.isAnnotationPresent(GroupBy.class)
                                || field.isAnnotationPresent(Max.class)
                                || field.isAnnotationPresent(Min.class)
                                || field.isAnnotationPresent(Sum.class)
                ) {
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

    public static List<Field> getNonStaticFields(Class type) {
        return tryGetFieldsFromCache(type, Modifier.STATIC);
    }

    /**
     * 获取字段列表，以包括所有父对象的子段
     * <p>
     * 不包括
     *
     * @param type
     * @return
     */
    public static List<Field> tryGetFieldsFromCache(Class type, int excludeModifiers) {

//        if (type == null) {
//            return Collections.emptyList();
//        }

        List<Field> fields = cacheFields.get(type.getName());

        if (fields == null) {

            fields = getFields(type, excludeModifiers);

            //放入不可变的列表
            cacheFields.put(type.getName(), Collections.unmodifiableList(fields));
        }

        return fields;
    }


    /**
     * 递归获取所有的字段，包括父类的字段
     *
     * @param type
     * @return
     */
    static synchronized List<Field> getFields(Class type, int excludeModifiers) {

        if (type == null
                || isRootObjectType(type)
                || isPrimitive(type)
                || isArray(type)
                || isIgnore(type)) {

            return Collections.emptyList();

        }

        List<Field> fields = new ArrayList<>(16);

        //先加入父类的字段
        Class superclass = type.getSuperclass();

        if (superclass != null) {
            fields.addAll(tryGetFieldsFromCache(superclass, excludeModifiers));
        }

        for (Field field : type.getDeclaredFields()) {
            //如果不是被过滤的类型
            if ((field.getModifiers() & excludeModifiers) == 0) {
                fields.add(field);
            }
        }

        return fields;

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
                && !QueryAnnotationUtil.isPrimitive(varType)
                && !varType.isArray()
//                && !Object[].class.isAssignableFrom(varType)
                && !Map.class.isAssignableFrom(varType) //并且不是 Map
                && !Iterable.class.isAssignableFrom(varType) //并且不是可迭代对象
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
                && (Object.class == type || Object.class.getName().equals(((Class) type).getName()));
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
    public static boolean isPrimitive(Class type) {
        return BeanUtils.isSimpleProperty(type);
    }

    public static boolean isIgnore(Class clazz) {
        return clazz.isAnnotationPresent(Ignore.class);
    }

    public static boolean isIgnore(Field field) {
        return field.isAnnotationPresent(Ignore.class);
    }

}
