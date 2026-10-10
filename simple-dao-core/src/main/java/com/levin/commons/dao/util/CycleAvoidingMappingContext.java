package com.levin.commons.dao.util;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** 一次对象映射内复用已创建的目标对象，避免双向关联递归。 */
public class CycleAvoidingMappingContext {

    private final Map<Object, Map<Class<?>, Object>> mappedInstances = new IdentityHashMap<>();

    public <T> T getMappedInstance(Object source, Class<T> targetType) {
        Map<Class<?>, Object> targets = mappedInstances.get(source);
        return targets == null ? null : targetType.cast(targets.get(targetType));
    }

    public void storeMappedInstance(Object source, Object target, Class<?> targetType) {
        mappedInstances.computeIfAbsent(source, key -> new LinkedHashMap<>()).put(targetType, target);
    }

    public void clear() {
        mappedInstances.clear();
    }
}
