package com.levin.commons.dao.util;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** 在读取实体 getter 之前检查属性是否已经加载。 */
public final class HibernateLazyPropertyUtil {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    private HibernateLazyPropertyUtil() {
    }

    public static boolean isLoaded(Object entity, String propertyName) {
        return entity == null || PERSISTENCE_UTIL.isLoaded(entity, propertyName);
    }

    public static boolean shouldMap(Object entity, String propertyName, boolean allowLazyLoading) {
        if (entity == null || (!allowLazyLoading && !isLoaded(entity, propertyName))) {
            return false;
        }

        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(entity.getClass(), propertyName);
        if (descriptor != null && descriptor.getReadMethod() != null) {
            Method readMethod = descriptor.getReadMethod();
            ReflectionUtils.makeAccessible(readMethod);
            return ReflectionUtils.invokeMethod(readMethod, entity) != null;
        }

        Field field = ReflectionUtils.findField(entity.getClass(), propertyName);
        if (field == null) {
            throw new IllegalArgumentException("No readable property: " + entity.getClass().getName() + "." + propertyName);
        }
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, entity) != null;
    }
}
