package com.levin.commons.dao.util;

import com.levin.commons.dao.DeepCopy;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.exception.PropertyNotFoundException;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.support.VariableInjector;
import com.levin.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.datetime.DateTimeFormatAnnotationFormatterFactory;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * DAO 层 Bean 拷贝工具备用入口。
 * <p>
 * 保持和 {@link ObjectUtil} 相同的公开入口，并为 DAO 查询结果转换场景增加 Hibernate lazy 安全读取。
 */
public abstract class DaoBeanCopyUtils {

    private static final Logger logger = LoggerFactory.getLogger(DaoBeanCopyUtils.class);

    private static final AnnotationFormatterFactory<DateTimeFormat> dateFormatterFactory = new DateTimeFormatAnnotationFormatterFactory();

    private static final AnnotationFormatterFactory<NumberFormat> numberFormatterFactory = new NumberFormatAnnotationFormatterFactory();

    private static volatile Method hibernateIsInitializedMethod;

    private static volatile boolean hibernateLookupDone;

    private static final Map<Class<?>, List<CopyFieldPlan>> copyFieldPlanCache = new ConcurrentHashMap<>();

    private static final Map<PropertyAccessKey, PropertyAccessPlan> propertyAccessPlanCache = new ConcurrentHashMap<>();

    private static final Map<String, String[]> propertyPathCache = new ConcurrentHashMap<>();

    private enum LazyValue {
        UNINITIALIZED
    }

    public static final ConfigurableConversionService conversionService = ObjectUtil.conversionService;

    public static final ThreadLocal<List<Predicate<String>>> fetchPropertiesFilters = ObjectUtil.fetchPropertiesFilters;

    public static final ThreadLocal<VariableInjector> VARIABLE_INJECTOR_THREAD_LOCAL = ObjectUtil.VARIABLE_INJECTOR_THREAD_LOCAL;

    public static <T> T copyProperties(Object source, T target, int deepLevel, String... ignoreProperties) {
        if (target instanceof Map) {
            if (source instanceof Map) {
                ((Map) target).putAll((Map) source);
            } else {
                copyProps2Map(source, (Map) target);
            }

            return target;
        }

        boolean isType = target instanceof Class;

        return (T) copy(source, (isType ? null : target), (isType ? (Class) target : null), deepLevel, ignoreProperties);
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
            return target == null ? rValue : target;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T convert(Object source, Class<T> targetType) {
        return ObjectUtil.convert(source, targetType);
    }

    public static Map copyField2Map(Object bean, Map map) {
        return ObjectUtil.copyField2Map(bean, map);
    }

    public static Map copyProps2Map(Object bean, Map map) {
        return ObjectUtil.copyProps2Map(bean, map);
    }

    public static Object setObjectValue(Object entity, Object fieldOrMethod, Object value) {
        return ObjectUtil.setObjectValue(entity, fieldOrMethod, value);
    }

    public static <T> T getIndexValue(Object source, String propertyName) {
        return ObjectUtil.getIndexValue(source, propertyName);
    }

    public static <T> T findValue(String key, boolean findLast, boolean isThrowExWhenKeyNotFound, List<Map<String, ? extends Object>> contexts) {
        return ObjectUtil.findValue(key, findLast, isThrowExWhenKeyNotFound, contexts);
    }

    public static <T> T getIndexValue(Object source, String propertyName, boolean isThrowExWhenPropertyNotFound) {
        return ObjectUtil.getIndexValue(source, propertyName, isThrowExWhenPropertyNotFound);
    }

    public static <T> T getValue(Object source, String propertyName, boolean isThrowExWhenPropertyNotFound) {
        return ObjectUtil.getValue(source, propertyName, isThrowExWhenPropertyNotFound);
    }

    public static <T> T evalSpEL(Object rootObject, String expression, Map<String, Object>... contexts) {
        return ObjectUtil.evalSpEL(rootObject, expression, contexts);
    }

    public static boolean isIgnore(String path, Object source, Object target, Field field, Class fieldType, String... ignoreProperties) {
        return ObjectUtil.isIgnore(path, source, target, field, fieldType, ignoreProperties);
    }

    public static <T> T tryToNewCollectionInstance(Class<T> targetType, Class sourceType, Class eleType, int n)
            throws IllegalAccessException, InstantiationException {
        return ObjectUtil.tryToNewCollectionInstance(targetType, sourceType, eleType, n);
    }

    public static <T extends Map> T tryToNewMapInstance(Class<T> targetType, Class sourceType) {
        return ObjectUtil.tryToNewMapInstance(targetType, sourceType);
    }

    public static Collection tryToGetElements(Object source) {
        return ObjectUtil.tryToGetElements(source);
    }

    public static <T> T copy(Object source, T target, Class<T> targetType,
                             ResolvableType ownerResolvableType,
                             Map<Field, Throwable> copyErrors,
                             String propertyPath,
                             Stack objectStack,
                             int invokeDeep,
                             int maxCopyDeep,
                             String... ignoreProperties) throws Exception {
        if (source == null || isLazyUninitialized(source)) {
            return null;
        }

        if (targetType == null && target != null) {
            targetType = (Class<T>) target.getClass();
        }

        if (targetType == null) {
            throw new IllegalArgumentException("targetType and target is null");
        }

        final ResolvableType myResolvableType = ResolvableType.forType(targetType, ownerResolvableType);
        targetType = (Class<T>) myResolvableType.resolve(targetType);

        if (myResolvableType.resolve() == null) {
            throw new IllegalStateException(targetType.getName() + " found Unresolvable generics");
        }

        if (propertyPath == null) {
            propertyPath = "";
        }

        if (isIgnore(propertyPath, source, target, null, null, ignoreProperties)) {
            return target;
        }

        if (BeanUtils.isSimpleValueType(targetType)) {
            return convert(source, targetType);
        }

        if (invokeDeep < 1) {
            invokeDeep = 1;
        }

        if (maxCopyDeep > 0 && invokeDeep > maxCopyDeep) {
            throw new WarnException(propertyPath + " copy deep over max num " + maxCopyDeep);
        }

        if (objectStack == null) {
            objectStack = new Stack();
        }

        ObjectUtil.ObjectHolder objectHolder = new ObjectUtil.ObjectHolder(source);

        if (objectStack.contains(objectHolder)) {
            throw new IllegalArgumentException(propertyPath + " copy object is endless loop " + source.getClass() + "#" + objectHolder.hashCode());
        }

        objectStack.push(objectHolder);

        try {
            if (targetType == Object.class || targetType.getName().equals(Object.class.getName())) {
                return (T) source;
            } else if (targetType.isArray() || Collection.class.isAssignableFrom(targetType)) {
                if (source instanceof CharSequence) {
                    source = source.toString().split(",");
                }

                Collection elements = tryToGetElements(source);

                if (elements == null) {
                    throw new IllegalArgumentException(propertyPath + " require " + (targetType.isArray() ? "Array" : targetType) + " but found " + source.getClass());
                }

                ResolvableType eleResolvableType = getCollectionEleType(myResolvableType);
                Class eleType = eleResolvableType != null ? eleResolvableType.resolve() : null;

                if (Object.class == eleType) {
                    eleType = null;
                }

                target = tryToNewCollectionInstance(targetType, source.getClass(), eleType, elements.size());

                int index = 0;

                for (Object element : elements) {
                    boolean notCopy = (eleType == null || element == null || isLazyUninitialized(element));

                    Object copyObject = notCopy ? (isLazyUninitialized(element) ? null : element) : copy(element, null, eleType,
                            getType(eleResolvableType), copyErrors, propertyPath + "[" + index + "]",
                            objectStack, invokeDeep + 1, maxCopyDeep, ignoreProperties);

                    if (target instanceof Collection) {
                        ((Collection) target).add(copyObject);
                    } else if (target.getClass().isArray()) {
                        java.lang.reflect.Array.set(target, index, copyObject);
                    } else {
                        throw new IllegalArgumentException("target is not a collection instance");
                    }

                    index++;
                }

                return target;
            } else if (Map.class.isAssignableFrom(targetType)) {
                if (!(source instanceof Map)) {
                    throw new IllegalArgumentException(propertyPath + " require a map , but copy source is " + source.getClass().getName());
                }

                Map<?, ?> sourceMap = (Map) source;
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

                    Object key = (keyType == null || entryKey == null || isLazyUninitialized(entryKey)) ? entryKey : copy(entryKey, null, keyType, keyRT, copyErrors, newPath, objectStack, invokeDeep + 1, maxCopyDeep, ignoreProperties);

                    if (key == null) {
                        index++;
                        continue;
                    }

                    Object value = (valueType == null || entryValue == null || isLazyUninitialized(entryValue)) ? (isLazyUninitialized(entryValue) ? null : entryValue) : copy(entryValue, null, valueType, valueRT, copyErrors, newPath, objectStack, invokeDeep + 1, maxCopyDeep, ignoreProperties);

                    targetMap.put(key, value);
                    index++;
                }

                return target;
            }

            if (target == null) {
                if (targetType.isInterface() || Modifier.isAbstract(targetType.getModifiers())) {
                    return (T) source;
                }

                target = BeanUtils.instantiateClass(targetType);
            }

            List<CopyFieldPlan> fieldPlans = getCopyFieldPlans(targetType);

            for (CopyFieldPlan fieldPlan : fieldPlans) {
                Field field = fieldPlan.field;
                String fieldPropertyPath = "";

                try {
                    String propertyName = fieldPlan.propertyName;

                    if (fieldPlan.fetchBindToField) {
                        if (Optional.ofNullable(fetchPropertiesFilters.get())
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(Objects::nonNull)
                                .noneMatch(predicate -> predicate.test(fieldPlan.fetchKey))) {
                            continue;
                        }
                    }

                    int fieldMaxCopyDeep = fieldPlan.maxCopyDeep != null ? fieldPlan.maxCopyDeep : maxCopyDeep;
                    String[] fieldIgnoreProperties = fieldPlan.ignoreProperties != null ? fieldPlan.ignoreProperties : ignoreProperties;

                    ResolvableType fieldResolvableType = ResolvableType.forField(field, myResolvableType);

                    if (fieldResolvableType.resolve() == null) {
                        throw new IllegalStateException(field + " found unresolvable generics");
                    }

                    final Class fieldType = fieldResolvableType.resolve(field.getType());

                    fieldPropertyPath = buildDeepPath(propertyPath, field.getName());

                    if (isIgnore(fieldPropertyPath, source, target, field, fieldType, ignoreProperties)) {
                        continue;
                    }

                    if (invokeDeep > 5 && invokeDeep % 3 == 0) {
                        logger.warn("*** 递归拷贝调用层次过多 [" + fieldPropertyPath + "], 调用层次：" + invokeDeep + " ，当前字段：" + field);
                    }

                    Object value;

                    final VariableInjector variableInjector = VARIABLE_INJECTOR_THREAD_LOCAL.get();

                    if (variableInjector != null && variableInjector.isDomainMatch(field)) {
                        variableInjector.injectValueByBean(target, field, source);
                        continue;
                    } else {
                        value = getIndexValueForCopy(source, propertyName);

                        if (value == LazyValue.UNINITIALIZED) {
                            if (!fieldType.isPrimitive()) {
                                field.set(target, null);
                            }
                            continue;
                        }

                        value = convertDate(fieldType, fieldPlan.dateTimeFormat, value);
                        value = convertNumber(fieldType, fieldPlan.numberFormat, value);
                    }

                    if (value == null) {
                        if (!fieldType.isPrimitive()) {
                            field.set(target, null);
                        }
                    } else if (BeanUtils.isSimpleValueType(fieldType)) {
                        field.set(target, convert(value, fieldType));
                    } else {
                        field.set(target, copy(value, field.get(target), fieldType,
                                getType(fieldResolvableType), copyErrors, fieldPropertyPath,
                                objectStack, invokeDeep + 1, fieldMaxCopyDeep, fieldIgnoreProperties));
                    }
                } catch (PropertyNotFoundException ex) {
                    if (logger.isTraceEnabled()) {
                        String errInfo = String.format("Can't copy [%s], error: %s", field.getDeclaringClass().getName() + "." + field.getName(), ex.getMessage());
                        logger.trace(errInfo);
                    }
                } catch (Exception ex) {
                    if (copyErrors != null) {
                        copyErrors.put(field, ex);
                    } else {
                        if (ex instanceof WarnException || ex.getClass().getName().startsWith("org.hibernate.")) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("Can't copy [%s], error: %s", field.getDeclaringClass().getName() + "." + field.getName(), ex.getMessage()));
                            }
                        } else {
                            logger.error(String.format("Can't copy [%s], error: %s", field.getDeclaringClass().getName() + "." + field.getName(), ex.getMessage()));
                        }
                    }
                } catch (StackOverflowError error) {
                    String errInfo = String.format("StackOverflowError Can't copy [%s] from [ %s] , error:%s",
                            fieldPropertyPath, field, ExceptionUtils.getAllCauseInfo(error, "->"));

                    logger.error(errInfo, error);
                }
            }

            return target;
        } finally {
            if (!objectStack.empty()) {
                objectStack.pop();
            }
        }
    }

    public static String buildDeepPath(String path, String propertyName) {
        return ObjectUtil.buildDeepPath(path, propertyName);
    }

    private static List<CopyFieldPlan> getCopyFieldPlans(Class<?> targetType) {
        return copyFieldPlanCache.computeIfAbsent(targetType, type -> {
            List<Field> fields = QueryAnnotationUtil.getNonStaticFields(type);
            List<CopyFieldPlan> plans = new ArrayList<>(fields.size());

            for (Field field : fields) {
                field.setAccessible(true);

                String propertyName = field.getName();

                Desc desc = field.getAnnotation(Desc.class);
                if (desc != null && hasText(desc.code())) {
                    propertyName = desc.code();
                }

                Fetch fetch = field.getAnnotation(Fetch.class);
                boolean fetchBindToField = false;
                String fetchKey = null;

                if (fetch != null) {
                    propertyName = hasText(fetch.value()) ? fetch.value() : propertyName;
                    fetchBindToField = fetch.isBindToField();
                    fetchKey = field.getDeclaringClass().getName() + "|" + propertyName;
                }

                Integer maxCopyDeep = null;
                String[] ignoreProperties = null;

                DeepCopy deepCopy = field.getAnnotation(DeepCopy.class);
                if (deepCopy != null && hasText(deepCopy.value())) {
                    propertyName = deepCopy.value();
                    maxCopyDeep = deepCopy.maxCopyDeep();
                    ignoreProperties = deepCopy.ignoreProperties();
                }

                plans.add(new CopyFieldPlan(field, propertyName, fetchBindToField, fetchKey,
                        maxCopyDeep, ignoreProperties,
                        field.getAnnotation(DateTimeFormat.class),
                        field.getAnnotation(NumberFormat.class)));
            }

            return Collections.unmodifiableList(plans);
        });
    }

    private static Object getIndexValueForCopy(Object source, String propertyName) {
        String[] names = getPropertyPath(propertyName);
        Object result = null;

        for (String name : names) {
            if (source != null) {
                source = result = getValueForCopy(source, name, true);

                if (source == LazyValue.UNINITIALIZED) {
                    return LazyValue.UNINITIALIZED;
                }
            }
        }

        return result;
    }

    private static String[] getPropertyPath(String propertyName) {
        return propertyPathCache.computeIfAbsent(propertyName, key -> Arrays.stream(key.split("\\."))
                .map(String::trim)
                .filter(DaoBeanCopyUtils::hasText)
                .toArray(String[]::new));
    }

    private static boolean hasText(String value) {
        return org.springframework.util.StringUtils.hasText(value);
    }

    private static Object getValueForCopy(Object source, String propertyName, boolean isThrowExWhenPropertyNotFound) {
        if (source == null) {
            if (isThrowExWhenPropertyNotFound) {
                throw new PropertyNotFoundException("key [" + propertyName + "] not found on null object");
            }

            return null;
        }

        if (isLazyUninitialized(source)) {
            return LazyValue.UNINITIALIZED;
        }

        if (source instanceof Map) {
            Map map = Map.class.cast(source);

            if (isThrowExWhenPropertyNotFound && !map.containsKey(propertyName)) {
                throw new PropertyNotFoundException("key [" + propertyName + "] not found in map");
            }

            Object value = map.get(propertyName);
            return isLazyUninitialized(value) ? LazyValue.UNINITIALIZED : value;
        }

        PropertyAccessPlan accessPlan = getPropertyAccessPlan(source.getClass(), propertyName);

        if (accessPlan.field != null) {
            try {
                Object value = accessPlan.field.get(source);
                if (isLazyUninitialized(value)) {
                    return LazyValue.UNINITIALIZED;
                }
            } catch (IllegalAccessException e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        }

        if (accessPlan.readMethod != null) {
            try {
                Object value = accessPlan.readMethod.invoke(source);
                return isLazyUninitialized(value) ? LazyValue.UNINITIALIZED : value;
            } catch (Exception e) {
                Throwable cause = ExceptionUtils.getCauseByStartsWith(e, "org.hibernate.");

                if (cause != null) {
                    return LazyValue.UNINITIALIZED;
                }

                ReflectionUtils.rethrowRuntimeException(e);
            }
        }

        if (accessPlan.field != null) {
            try {
                Object value = accessPlan.field.get(source);
                return isLazyUninitialized(value) ? LazyValue.UNINITIALIZED : value;
            } catch (IllegalAccessException e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        } else if (isThrowExWhenPropertyNotFound) {
            throw new PropertyNotFoundException(source.getClass() + " can't find property:" + propertyName);
        }

        return null;
    }

    private static PropertyAccessPlan getPropertyAccessPlan(Class<?> sourceType, String propertyName) {
        return propertyAccessPlanCache.computeIfAbsent(new PropertyAccessKey(sourceType, propertyName), key -> {
            Field field = ReflectionUtils.findField(sourceType, propertyName);

            if (field != null) {
                field.setAccessible(true);
            }

            Method readMethod = null;

            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(sourceType, propertyName);
                if (pd != null) {
                    readMethod = pd.getReadMethod();
                }
            } catch (Exception e) {
            }

            if (readMethod != null) {
                readMethod.setAccessible(true);
            }

            return new PropertyAccessPlan(field, readMethod);
        });
    }

    private static final class CopyFieldPlan {
        final Field field;
        final String propertyName;
        final boolean fetchBindToField;
        final String fetchKey;
        final Integer maxCopyDeep;
        final String[] ignoreProperties;
        final DateTimeFormat dateTimeFormat;
        final NumberFormat numberFormat;

        CopyFieldPlan(Field field, String propertyName, boolean fetchBindToField, String fetchKey,
                      Integer maxCopyDeep, String[] ignoreProperties,
                      DateTimeFormat dateTimeFormat, NumberFormat numberFormat) {
            this.field = field;
            this.propertyName = propertyName;
            this.fetchBindToField = fetchBindToField;
            this.fetchKey = fetchKey;
            this.maxCopyDeep = maxCopyDeep;
            this.ignoreProperties = ignoreProperties;
            this.dateTimeFormat = dateTimeFormat;
            this.numberFormat = numberFormat;
        }
    }

    private static final class PropertyAccessPlan {
        final Field field;
        final Method readMethod;

        PropertyAccessPlan(Field field, Method readMethod) {
            this.field = field;
            this.readMethod = readMethod;
        }
    }

    private static final class PropertyAccessKey {
        final Class<?> sourceType;
        final String propertyName;

        PropertyAccessKey(Class<?> sourceType, String propertyName) {
            this.sourceType = sourceType;
            this.propertyName = propertyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof PropertyAccessKey)) {
                return false;
            }

            PropertyAccessKey that = (PropertyAccessKey) o;
            return Objects.equals(sourceType, that.sourceType) && Objects.equals(propertyName, that.propertyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceType, propertyName);
        }
    }

    private static boolean isLazyUninitialized(Object value) {
        return !isHibernateInitialized(value);
    }

    private static boolean isHibernateInitialized(Object value) {
        if (value == null) {
            return true;
        }

        Method method = getHibernateIsInitializedMethod();

        if (method == null) {
            return true;
        }

        try {
            Object result = method.invoke(null, value);
            return !Boolean.FALSE.equals(result);
        } catch (Exception e) {
            return true;
        }
    }

    private static Method getHibernateIsInitializedMethod() {
        if (!hibernateLookupDone) {
            synchronized (DaoBeanCopyUtils.class) {
                if (!hibernateLookupDone) {
                    try {
                        Class<?> hibernateClass = Class.forName("org.hibernate.Hibernate");
                        hibernateIsInitializedMethod = hibernateClass.getMethod("isInitialized", Object.class);
                    } catch (Exception e) {
                        hibernateIsInitializedMethod = null;
                    }

                    hibernateLookupDone = true;
                }
            }
        }

        return hibernateIsInitializedMethod;
    }

    private static Object convertNumber(Class fieldType, NumberFormat numberFormat, Object value) throws java.text.ParseException {
        if (value != null && numberFormat != null && !fieldType.isAssignableFrom(value.getClass())) {
            if (value instanceof CharSequence) {
                value = numberFormatterFactory.getParser(numberFormat, fieldType).parse(value.toString(), Locale.getDefault());
            } else if (fieldType.isAssignableFrom(String.class)) {
                Printer<Object> printer = (Printer<Object>) numberFormatterFactory.getPrinter(numberFormat, fieldType);
                value = printer.print(value, Locale.getDefault());
            }
        }

        return value;
    }

    private static Object convertDate(Class fieldType, DateTimeFormat dateTimeFormat, Object value) throws java.text.ParseException {
        if (value != null && dateTimeFormat != null && !fieldType.isAssignableFrom(value.getClass())) {
            if (value instanceof CharSequence) {
                value = dateFormatterFactory.getParser(dateTimeFormat, fieldType).parse(value.toString(), Locale.getDefault());
            } else if (fieldType.isAssignableFrom(String.class)) {
                Printer<Object> printer = (Printer<Object>) dateFormatterFactory.getPrinter(dateTimeFormat, fieldType);
                value = printer.print(value, Locale.getDefault());
            }
        }

        return value;
    }

    private static boolean hasGenerics(ResolvableType resolvableType) {
        if (resolvableType == null) {
            return false;
        }

        return resolvableType.hasGenerics()
                || (resolvableType.isArray() && resolvableType.getComponentType().hasGenerics());
    }

    private static ResolvableType getType(ResolvableType resolvableType) {
        return hasGenerics(resolvableType) ? resolvableType : null;
    }

    private static ResolvableType getCollectionEleType(ResolvableType resolvableType) {
        if (resolvableType.isArray()) {
            return resolvableType.getComponentType();
        }

        return resolvableType.hasGenerics() ? resolvableType.getGeneric(0) : null;
    }
}
