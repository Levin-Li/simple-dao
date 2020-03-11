package com.levin.commons.dao.util;


import com.levin.commons.dao.DeepCopy;
import com.levin.commons.service.domain.Desc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by echo on 2016/8/1.
 */
public abstract class ObjectUtil {

    private static final Logger logger = LoggerFactory.getLogger(ObjectUtil.class);


    public static final GenericConversionService conversionService = new DefaultConversionService();


    /**
     * 属性拷贝器
     * <p>
     *
     * @param source
     * @param target
     * @param ignoreProperties
     * @see @DeepCopy
     */
    public static Object copyProperties(Object source, Object target, int deepLevel, String... ignoreProperties) {

        if (target instanceof Map) {

            if (source instanceof Map)
                ((Map) target).putAll((Map) source);
            else
                copyProps2Map(source, (Map) target);

            return target;
        }


        //如果 target 是类型
        boolean isType = target instanceof Class;


        return copy(source, (isType ? null : target), (isType ? (Class) target : null), deepLevel, ignoreProperties);
    }


    public static <T> T copy(Object source, Class<T> targetType, String... ignoreProperties) throws RuntimeException {
        return copy(source, targetType, -1, ignoreProperties);
    }

    public static <T> T copy(Object source, Class<T> targetType, int maxCopyDeep, String... ignoreProperties) throws RuntimeException {
        return copy(source, null, targetType, maxCopyDeep, ignoreProperties);
    }


    public static <T> T copy(Object source, T target, Class<T> targetType, int maxCopyDeep, String... ignoreProperties) throws RuntimeException {
        try {

            T rValue = copy(source, target, targetType, null, null, null, null, 1, maxCopyDeep, ignoreProperties);

            //重要步骤，如果 target本身不Null,则返回 target 本身

            return target == null ? rValue : target;

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw ((RuntimeException) e);
            else
                throw new RuntimeException(e);
        }
    }

    /**
     * @param source
     * @param targetType
     * @param <T>
     * @return
     */
    public static <T> T convert(Object source, Class<T> targetType) {

        //对枚举类型进行转换
        if (targetType.isEnum()) {
            if (source == null)
                return null;
            else if (source instanceof Number)
                return targetType.getEnumConstants()[((Number) source).intValue()];
            else if (source instanceof CharSequence) {
                Class enumType = targetType;
                return (T) Enum.valueOf(enumType, "" + source);
            }
        }

        return conversionService.convert(source, targetType);

    }


    /**
     * 不包括静态字段，final字
     *
     * @param bean
     * @param map
     */
    public static Map copyField2Map(Object bean, Map map) {


        if (map == null) {
            map = new LinkedHashMap();
        }

        if (bean == null) {
            return map;
        }


        if (bean instanceof Map) {

            map.putAll((Map) bean);

            return map;
        }

        List<Field> fields = QueryAnnotationUtil.getCacheFields(bean.getClass());

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(bean));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    public static Map copyProps2Map(Object bean, Map map) {

        if (map == null || bean == null) {
            map = new LinkedHashMap();
        }


        if (bean instanceof Map) {

            map.putAll((Map) bean);

            return map;
        }

        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(bean.getClass());

        for (PropertyDescriptor pd : descriptors) {

            Method readMethod = pd.getReadMethod();

            if (readMethod == null
                    || readMethod.getDeclaringClass().getName().equals(Object.class.getName())) {
                continue;
            }

            try {
                map.put(pd.getName(), readMethod.invoke(bean));
            } catch (Exception e) {
                logger.warn("copy " + readMethod + " --> map", e);
            }
        }

        return map;
    }


    public static <T> T getIndexValue(Object source, String propertyName) {
        return getIndexValue(source, propertyName, true);
    }

    /**
     * 查找值
     *
     * @param key
     * @param contexts
     * @return
     */
    public static <T> T findValue(String key, boolean findLast, boolean isThrowExWhenKeyNotFound, List<Map<String, ? extends Object>> contexts) {

        if (findLast) {
            contexts = new ArrayList<>(contexts);
            Collections.reverse(contexts);
        }

        for (Map<String, ? extends Object> map : contexts) {

            if (map == null || map.isEmpty()) {
                continue;
            }

            try {
                return ObjectUtil.getIndexValue(map, key, true);
            } catch (Exception e) {
            }
        }


        if (isThrowExWhenKeyNotFound) {
            throw new IllegalArgumentException("key " + key + " not found on context");
        }

        return null;
    }

    /**
     * 获取对象的多级属性直
     * <p>
     * 支持info.type.name的方式获取属性
     *
     * @param source
     * @param propertyName
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getIndexValue(Object source, String propertyName, boolean isThrowExWhenPropertyNotFound) {

        String[] names = propertyName.split("\\.");

        Object result = null;

        String key = null;

        for (String name : names) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            key = name;

            if (source != null) {
                source = result = getValue(source, name.trim(), isThrowExWhenPropertyNotFound);

                //防止代理对象
                if (source instanceof Collection) {

                } else if (source instanceof Map) {

                }

                key = null;
            }
        }

        //如果属性没有取完整
//        if (key != null && isThrowExWhenPropertyNotFound) {
////            throw new IllegalArgumentException("propertyName " + propertyName);
////        }

        return (T) result;
    }


    /**
     * 获取对象的一级属性值
     *
     * @param source
     * @param propertyName
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getValue(Object source, String propertyName, boolean isThrowExWhenPropertyNotFound) {


        if (source == null) {

            if (isThrowExWhenPropertyNotFound) {
                throw new IllegalArgumentException("key [" + propertyName + "] not found on null object");
            }

            return null;
        }


        if (source instanceof Map) {

            Map map = Map.class.cast(source);

            if (isThrowExWhenPropertyNotFound && !map.containsKey(propertyName)) {
                throw new IllegalArgumentException("key [" + propertyName + "] not found in map");
            }

            return (T) map.get(propertyName);
        }

        PropertyDescriptor pd = null;

        try {
            pd = BeanUtils.getPropertyDescriptor(source.getClass(), propertyName);
        } catch (Exception e) {

        }

        //1、首先使用方法读取
        if (pd != null && pd.getReadMethod() != null) {

            Method readMethod = pd.getReadMethod();

            readMethod.setAccessible(true);

            try {
                return (T) readMethod.invoke(source);
            } catch (Exception e) {

                //如果是 hibernate 延迟加载错误
                if (ExceptionUtils.getCauseByStartsWith(e, "org.hibernate.") != null)
                    return null;

                ReflectionUtils.rethrowRuntimeException(e);
            }
        }

        Field field = ReflectionUtils.findField(source.getClass(), propertyName);

        if (field != null) {
            field.setAccessible(true);
            try {
                return (T) field.get(source);
            } catch (IllegalAccessException e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        } else if (isThrowExWhenPropertyNotFound) {
            throw new IllegalArgumentException(source.getClass() + " can't find property:" + propertyName);
        }

        return null;
    }


    /**
     * spring el 求值
     *
     * @param rootObject
     * @param expression
     * @param contexts
     * @param <T>
     * @return
     */
    public static <T> T evalSpEL(Object rootObject, String expression, Map<String, ? super Object>... contexts) {

        EvaluationContext ctx = new StandardEvaluationContext(rootObject);

        if (contexts != null) {
            for (Map<String, Object> context : contexts) {
                if (context != null) {
                    for (Map.Entry<String, Object> entry : context.entrySet()) {
                        ctx.setVariable(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        ExpressionParser parser = new SpelExpressionParser();

        return (T) parser.parseExpression(expression).getValue(ctx);

    }


    /**
     * 属性拷贝时，是否是忽略的字段
     *
     * @param path
     * @param source
     * @param target
     * @param field
     * @param fieldType
     * @param ignoreProperties
     * @return
     */
    public static boolean isIgnore(String path, Object source, Object target, Field field, Class fieldType, String... ignoreProperties) {

        if (!StringUtils.hasText(path)
                || ignoreProperties == null) {

            return false;
        }

        for (String ignoreProperty : ignoreProperties) {

            if (!StringUtils.hasText(ignoreProperty))
                continue;

            if (path.equals(ignoreProperty)
                    || ignoreProperty.equals(path + "*")) {
                return true;
            }

            // a.b.c.name*
            // a.b.c.{}

            //如果是复杂属性
            if (fieldType != null && !BeanUtils.isSimpleProperty(fieldType)) {
                if (ignoreProperty.equals("{*}")
                        || ignoreProperty.equals(path + ".{*}")
                        || ignoreProperty.equals(path + String.format(".{%s}", fieldType.getName())))
                    return true;
            }

            if (ignoreProperty.startsWith("spel:")) {

                ignoreProperty = ignoreProperty.substring(3).trim();

                if (!StringUtils.hasText(ignoreProperty)) {
                    continue;
                }

                Map<String, Object> vars = new HashMap<>();


                vars.put("source", source);
                vars.put("target", target);

                vars.put("path", path);
                vars.put("field", field);
                vars.put("fieldType", fieldType);

                Boolean result = evalSpEL(null, ignoreProperty, vars);

                if (result != null
                        && Boolean.TRUE.equals(result)) {
                    return true;
                }

            }
        }

        return false;
    }


    /**
     * 自动初始化集合类
     *
     * @param targetType
     * @param sourceType
     * @param eleType
     * @param n
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T tryToNewCollectionInstance(Class<T> targetType, Class sourceType, Class eleType, int n) throws IllegalAccessException, InstantiationException {

        if (targetType.isArray()) {

            return (T) Array.newInstance(eleType != null ? eleType : Object.class, n);

        } else if (Collection.class.isAssignableFrom(targetType)) {

            //首先尝试和拷贝源相同的集合类
            //只使用标准的java.util包集合
            //   && !sourceType.getName().startsWith("org.hibernate.")
            if (sourceType != null
                    && sourceType.getName().startsWith("java.util.")
                    && targetType.isAssignableFrom(sourceType)) {
                try {
                    return (T) sourceType.newInstance();
                } catch (Exception e) {
                }
            }

            //如果是接口或是抽象类
            if (targetType.isInterface()
                    || Modifier.isAbstract(targetType.getModifiers())
                    || !Modifier.isPublic(targetType.getModifiers())) {

                if (List.class.isAssignableFrom(targetType)) {
                    return (T) new ArrayList(n);
                } else if (Queue.class.isAssignableFrom(targetType)) {
                    return (T) new LinkedBlockingDeque();
                } else if (SortedSet.class.isAssignableFrom(targetType)) {
                    return (T) new TreeSet();
                } else if (Set.class.isAssignableFrom(targetType)) {
                    return (T) new HashSet();
                } else {
                    return (T) new ArrayList(n);
                }

            }

            //直接按原类型实例化
            return targetType.newInstance();// BeanUtils.instantiateClass(targetType);

        } else {
            throw new UnsupportedOperationException(targetType.getName() + " is not a collection type");
        }
    }

    public static <T extends Map> T tryToNewMapInstance(Class<T> targetType, Class sourceType) {

        if (Map.class.isAssignableFrom(targetType)) {
            //首先尝试和拷贝源相同的类
            //只使用标准的java.util包集合
            //   && !sourceType.getName().startsWith("org.hibernate.")

            if (sourceType != null
                    && targetType.isAssignableFrom(sourceType)
                    && sourceType.getName().startsWith("java.util.")
                    ) {
                try {
                    return (T) sourceType.newInstance();
                } catch (Exception e) {
                }
            }

            //如果是接口或是抽象类
            if (targetType.isInterface()
                    || Modifier.isAbstract(targetType.getModifiers())
                    || !Modifier.isPublic(targetType.getModifiers())) {

                return (T) new LinkedHashMap<>();

            }

            //直接按原类型实例化
            return BeanUtils.instantiateClass(targetType);

        } else {
            throw new UnsupportedOperationException(targetType.getName() + " is not a map type");
        }
    }


    /**
     * 尝试获取集合元素
     *
     * @param source
     * @return
     */
    public static Collection tryToGetElements(Object source) {


        if (source.getClass().isArray()) {

            int length = Array.getLength(source);

            ArrayList arrayList = new ArrayList(length);

            int i = 0;

            while (i < length) {
                arrayList.add(Array.get(source, i++));
            }

            return arrayList;

        } else if (source instanceof List) {
            return (List) source;
        } else if (source instanceof Collection) {
            return new ArrayList((Collection) source);
        } else {

            if (source instanceof Iterable)
                source = ((Iterable) source).iterator();

            if (source instanceof Iterator) {
                ArrayList arrayList = new ArrayList(7);
                while (((Iterator) source).hasNext()) {
                    arrayList.add(((Iterator) source).next());
                }
                return arrayList;
            }
        }

        return null;
    }


    private static boolean hasGenerics(ResolvableType resolvableType) {

        if (resolvableType == null)
            return false;

        return resolvableType.hasGenerics()
                || (resolvableType.isArray() && resolvableType.getComponentType().hasGenerics());
    }


    private static ResolvableType getType(ResolvableType resolvableType) {
        return hasGenerics(resolvableType) ? resolvableType : null;
    }

    private static ResolvableType getCollectionEleType(ResolvableType resolvableType) {

        if (resolvableType.isArray())
            return resolvableType.getComponentType();

        return resolvableType.hasGenerics() ? resolvableType.getGeneric(0) : null;
    }


    /**
     *
     * <p>
     * <p>
     * 若果是未知类型的值，将直接复制引用
     *
     * @param source
     * @param targetType
     * @param copyErrors
     * @param <T>
     * @return
     */

    /**
     * 支持递归拷贝
     * <p>
     * 若果是未知类型的值，将直接复制引用
     *
     * @param source              源对象
     * @param target              目标对象
     * @param targetType          目标类似
     * @param ownerResolvableType 当前泛型的上下文所有者
     * @param copyErrors          拷贝错误
     * @param propertyPath        当前的属性路径
     * @param objectStack         路径堆栈，用来防止出来对象的应用递归
     * @param invokeDeep          当前已经进行的属性深度
     * @param maxCopyDeep         最大的拷贝深度
     * @param ignoreProperties    忽略目标对象的属性
     *                            a.b.c.name* *号表示忽略以什么开头的属性
     *                            a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                            a.b.c.{com.User}    大括号表示忽略User类型属性
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T copy(Object source, T target, Class<T> targetType
            , ResolvableType ownerResolvableType
            , Map<Field, Throwable> copyErrors
            , String propertyPath
            , Stack objectStack, int invokeDeep, int maxCopyDeep
            , String... ignoreProperties) throws Exception {

        if (source == null)
            return null;

        if (targetType == null
                && target != null) {
            targetType = (Class<T>) target.getClass();
        }

        if (targetType == null)
            throw new IllegalArgumentException("targetType and target is null");

        ///////////////////////////////////////////////////////////////////
        final ResolvableType myResolvableType = ResolvableType.forType(targetType, ownerResolvableType);

        targetType = (Class<T>) myResolvableType.resolve(targetType);

        if (myResolvableType.resolve() == null) {
            throw new IllegalStateException(targetType.getName() + " found Unresolvable generics");
        }

        ///////////////////////////////////////////////////////

        if (propertyPath == null) {
            propertyPath = "";
        }
        //如果是忽略的属性，则直接返回原值
        if (isIgnore(propertyPath, source, target, null, null, ignoreProperties)) {
            return target;
        }
//////////////////////////////////////////////////////////////////////////////////////////////////////////


        //如果是原子属性，可以认为是同一级
        if (BeanUtils.isSimpleValueType(targetType)) //   || BeanUtils.isSimpleValueType(source.getClass())
        {
            return convert(source, targetType);
        }

////////////////////////////////////////////////////////////////////

        //如果是原子属性，可以认为是同一级
        if (invokeDeep < 1) {
            invokeDeep = 1;
        }

        if (maxCopyDeep > 0
                && invokeDeep > maxCopyDeep) {
            //如果超出拷贝层数，买家返回Null
            // return null;
            throw new WarnException(propertyPath + " copy deep over max num " + maxCopyDeep);
        }
///////////////////////////////////////////////////////////////

        if (objectStack == null) {
            objectStack = new Stack();
        }


        ObjectHolder objectHolder = new ObjectHolder(source);

        if (objectStack.contains(objectHolder)) {
            //出现死循环
            throw new IllegalArgumentException(propertyPath + " copy object is endless loop " + source.getClass() + "#" + objectHolder.hashCode());
        } else {
            objectStack.push(objectHolder);
        }

/////////////////////////////////////////////////////////////////////////////////////////////
        //如果是基本类型直接返回
        if (targetType == Object.class
                || targetType.getName().equals(Object.class.getName())) {

            return (T) source;

        } else if (targetType.isArray() || Collection.class.isAssignableFrom(targetType)) {

            if (source instanceof CharSequence)
                source = source.toString().split(",");

            //可以考虑自动，转化单个对象为数组
            Collection elements = tryToGetElements(source);

            if (elements == null) {
                //如果发现需要的对象是数组或是集合，但是拷贝源对象不是集合或数组，抛出异常
                throw new IllegalArgumentException(propertyPath + " require " + (targetType.isArray() ? "Array" : targetType) + " but found " + source.getClass());
            }

            ResolvableType eleResolvableType = getCollectionEleType(myResolvableType);

            //获取集合元素类型
            Class eleType = eleResolvableType != null ? eleResolvableType.resolve() : null;

            if (Object.class == eleType)
                eleType = null;

            //自己重新赋值，忽略旧值
            target = tryToNewCollectionInstance(targetType, source.getClass(), eleType, elements.size());

            int index = 0;

            for (Object element : elements) {
                //如果没有具体的类型，则直接复制引用，否则递归拷贝

                boolean notCopy = (eleType == null || element == null);

                Object copyObject = notCopy ? element : copy(element, null, eleType
                        , getType(eleResolvableType)
                        , copyErrors, propertyPath + "[" + index + "]",
                        objectStack, invokeDeep, maxCopyDeep,
                        ignoreProperties);

                if (target instanceof Collection) {
                    ((Collection) target).add(copyObject);
                } else if (target.getClass().isArray()) {
                    Array.set(target, index, copyObject);
                } else {
                    throw new IllegalArgumentException("target is not a collection instance");
                }

                index++;
            }

            if (!objectStack.isEmpty()) {
                objectStack.pop();
            }

            return target;

        } else if (Map.class.isAssignableFrom(targetType)) {
            //
            //如果源不是Map
            if (!(source instanceof Map)) {
                throw new IllegalArgumentException(propertyPath + " require a map , but copy source is " + source.getClass().getName());
            }

            Map<?, ?> sourceMap = (Map) source;

            //如果是Map
            ResolvableType keyRT = myResolvableType.getGeneric(0);
            ResolvableType valueRT = myResolvableType.getGeneric(1);

            Class keyType = keyRT.resolve();
            Class valueType = valueRT.resolve();


            Map targetMap = tryToNewMapInstance((Class<Map>) targetType, source.getClass());
            target = (T) targetMap;

            int index = 0;

            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {

                String newPath = propertyPath + "[" + index + "]";

                Object entryKey = entry.getKey();
                Object entryValue = entry.getValue();

                Object key = (keyType == null || entryKey == null) ? entryKey : copy(entryKey, null, keyType, keyRT, copyErrors, newPath, objectStack, invokeDeep, maxCopyDeep, ignoreProperties);

                if (key == null) {
                    index++;
                    continue;
                }

                Object value = (valueType == null || entryValue == null) ? entryValue : copy(entryValue, null, valueType, valueRT, copyErrors, newPath, objectStack, invokeDeep, maxCopyDeep, ignoreProperties);

                targetMap.put(key, value);

                index++;

            }

            if (!objectStack.isEmpty()) {
                objectStack.pop();
            }

            return target;
        }

        ////////////////////////////////////////////////////////////////////////////////

        //如果不是集合，不是Map，也不是原子类型，则开始解析字段

        if (target == null) {
            //如果是抽象类，或是接口
            if (targetType.isInterface()
//                    || !Modifier.isPublic(targetType.getModifiers())
                    || Modifier.isAbstract(targetType.getModifiers())) {

                if (!objectStack.empty())
                    objectStack.pop();

                return (T) source;
            }

            //尝试实例话
            target = BeanUtils.instantiateClass(targetType);
        }

        final List<Field> fieldList = new ArrayList<>(15);

        ReflectionUtils.doWithFields(targetType, field -> {
            if (!Modifier.isStatic(field.getModifiers())) {
                fieldList.add(field);
            }
        });

        //按字段复制
        for (Field field : fieldList) {

            try {
                field.setAccessible(true);

                String propertyName = field.getName();

                Desc desc = field.getAnnotation(Desc.class);

                if (desc != null && StringUtils.hasText(desc.code())) {
                    propertyName = desc.code();
                }


                DeepCopy deepCopy = field.getAnnotation(DeepCopy.class);

                if (deepCopy != null && StringUtils.hasText(deepCopy.value())) {
                    propertyName = deepCopy.value();
                }


                ResolvableType fieldResolvableType = ResolvableType.forField(field, myResolvableType);

                if (fieldResolvableType.resolve() == null) {
                    throw new IllegalStateException(field + " found unresolvable generics");
                }

                Class fieldType = fieldResolvableType.resolve(field.getType());

                //如果是忽略的属性
                //isIgnore(String path, Object source, Object target, Field field, Class fieldType, String... ignoreProperties)

                final String fieldPropertyPath = buildDeepPath(propertyPath, field.getName());

                if (isIgnore(fieldPropertyPath, source, target, field, fieldType, ignoreProperties)) {
                    continue;
                }

                if (invokeDeep > 5) {
                    logger.warn("*** 递归拷贝调用层次过多 "+field + " " + fieldPropertyPath + " " + invokeDeep);
                }

                Object value = getIndexValue(source, propertyName);

                boolean isSimpleType = BeanUtils.isSimpleValueType(fieldType);

                if (value == null) {
                    //优化处理，直接返回
                    if (!isSimpleType)
                        field.set(target, null);
                } else if (isSimpleType) { //  || BeanUtils.isSimpleValueType(value.getClass())
                    //优化处理，直接返回
                    field.set(target, convert(value, fieldType));
                } else {
                    //递归拷贝属性
                    field.set(target, copy(value, field.get(target), fieldType
                            , getType(fieldResolvableType)
                            , copyErrors, fieldPropertyPath,
                            objectStack, invokeDeep + 1, maxCopyDeep,
                            ignoreProperties));
                }

            } catch (Exception e) {
                if (copyErrors != null) {
                    copyErrors.put(field, e);
                } else {
                    String errInfo = String.format("Can't copy [%s] from %s , error:%s", propertyPath, field
                            , ExceptionUtils.getAllCauseInfo(e, "->"));

                    if (e instanceof WarnException || e.getClass().getName().startsWith("org.hibernate.")) {
                        logger.warn(errInfo);
                    } else {
                        logger.error(errInfo);
                    }
                }
            } catch (StackOverflowError error) {
                String errInfo = String.format("StackOverflowError Can't copy [%s] from %s , error:%s"
                        , propertyPath, field, ExceptionUtils.getAllCauseInfo(error, "->"));
                logger.error(errInfo, error);
            }
        }

        if (!objectStack.empty()) {
            objectStack.pop();
        }

        return target;
    }

    /**
     * 获取泛型类型
     *
     * @param targetType
     * @param <T>
     * @return
     */

    private static <T> Class findElementType(Class<T> targetType) {

        ResolvableType resolvableType = ResolvableType.forClass(targetType);

        boolean hasGenerics = resolvableType.hasGenerics();

        Class eleType = null;

        if (targetType.isArray()) {

            eleType = hasGenerics ? resolvableType.resolveGeneric(0) : targetType.getComponentType();

        } else if (Collection.class.isAssignableFrom(targetType)) {
            eleType = hasGenerics ? resolvableType.resolveGeneric(0) : null;

        } else if (Map.class.isAssignableFrom(targetType)) {
            throw new UnsupportedOperationException(targetType.getName());
        }

        return eleType;

    }


    static class ObjectHolder {

        Object refObj;

        public ObjectHolder(Object refObj) {
            this.refObj = refObj;
        }

        //使用引用相等比较
        @Override
        public boolean equals(Object obj) {
            return refObj == obj;
        }

    }


    public static String buildDeepPath(String path, String propertyName) {
        return StringUtils.hasText(path) ? (path + "." + propertyName) : propertyName;
    }


}
