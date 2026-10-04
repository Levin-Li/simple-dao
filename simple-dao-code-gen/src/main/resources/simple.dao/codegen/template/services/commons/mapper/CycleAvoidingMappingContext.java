package ${modulePackageName}.services.commons.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** 一次实体映射内复用已创建的目标对象，避免双向关联递归。 */
public class CycleAvoidingMappingContext {

    private final Map<Object, Map<Class<?>, Object>> mappedInstances = new IdentityHashMap<>();

    @BeforeMapping
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        Map<Class<?>, Object> targets = mappedInstances.get(source);
        return targets == null ? null : targetType.cast(targets.get(targetType));
    }

    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target,
                                    @TargetType Class<?> targetType) {
        mappedInstances.computeIfAbsent(source, key -> new HashMap<>()).put(targetType, target);
    }
}
