package com.levin.commons.dao.codegen.model;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.GenDaoLogicGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Transient;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/** 已校验的实体逻辑分组字段，用于生成查询请求对象。 */
public final class DaoLogicGroupField {

    private static final Set<Class<? extends Annotation>> FILTERS = Set.of(
            Eq.class, NotEq.class, Gt.class, Gte.class, Lt.class, Lte.class,
            Like.class, NotLike.class, Contains.class, NotContains.class,
            StartsWith.class, NotStartsWith.class, EndsWith.class, NotEndsWith.class,
            In.class, NotIn.class);

    private final String sourceFieldName;
    private final String queryFieldName;
    private final String queryTypeName;
    private final DaoLogicGroup groupDefinition;
    private final String filterAnnotationName;
    private final String injectVarName;
    private final String injectOverride;
    private final String injectRequired;
    private final String title;
    private final GenDaoLogicGroup.ScopeRole scopeRole;

    private DaoLogicGroupField(Field field, DaoLogicGroup groupDefinition, GenDaoLogicGroup generation) {
        Class<? extends Annotation> filterOp = generation.filterOp();
        if (!FILTERS.contains(filterOp))
            throw new IllegalArgumentException("Unsupported DAO logic group filterOp: " + filterOp);
        if (field.isAnnotationPresent(EmbeddedId.class)
                || field.isAnnotationPresent(Transient.class) || Modifier.isTransient(field.getModifiers()))
            throw new IllegalArgumentException("DAO logic group requires a persistent scalar field: " + field);
        Class<?> fieldType = field.getType();
        if (!BeanUtils.isSimpleValueType(fieldType) && !fieldType.isEnum())
            throw new IllegalArgumentException("DAO logic group requires a scalar field: " + field);

        Class<?> queryType = boxed(fieldType);
        this.sourceFieldName = field.getName();
        this.queryFieldName = StringUtils.uncapitalize(filterOp.getSimpleName())
                + StringUtils.capitalize(field.getName())
                + ((filterOp == In.class || filterOp == NotIn.class) ? "List" : "");
        this.queryTypeName = (filterOp == In.class || filterOp == NotIn.class)
                ? (queryType == String.class && generation.scopeRole() != GenDaoLogicGroup.ScopeRole.NONE ? "List<String>" : "java.util.List<" + queryType.getCanonicalName() + ">") : queryType.getCanonicalName();
        this.groupDefinition = groupDefinition;
        this.filterAnnotationName = filterOp.getSimpleName();
        this.injectVarName = StringUtils.hasText(generation.injectVarName())
                ? generation.injectVarName().trim() : field.getName();
        this.injectOverride = generation.injectOverride();
        this.injectRequired = generation.injectRequired();
        Schema schema = field.getAnnotation(Schema.class);
        this.title = schema != null && StringUtils.hasText(schema.title()) ? schema.title() : field.getName();
        this.scopeRole = generation.scopeRole();
        if (scopeRole == GenDaoLogicGroup.ScopeRole.TENANT || scopeRole == GenDaoLogicGroup.ScopeRole.PLATFORM_DOMAIN)
            throw new IllegalArgumentException("Tenant and platform-domain scope generation is not supported yet: " + field);
        if (scopeRole != GenDaoLogicGroup.ScopeRole.NONE) {
            if (filterOp != In.class || fieldType != String.class
                    || !"true".equals(generation.injectOverride().trim())
                    || groupDefinition.logicOp() != DaoLogicGroup.LogicOp.OR
                    || groupDefinition.minConditions() < 1
                    || !"1=2".equals(groupDefinition.unless().replaceAll("\\s+", ""))
                    || StringUtils.hasText(groupDefinition.condition())
                    || StringUtils.hasText(groupDefinition.parent()))
                throw new IllegalArgumentException("Participant scope requires a root, fail-closed OR/IN group on a String field: " + field);
        }
    }

    public static List<DaoLogicGroupField> forType(Class<?> entityClass) {
        Map<String, DaoLogicGroup> definitions = definitionsForType(entityClass);
        Map<String, Field> fields = new LinkedHashMap<>();
        ReflectionUtils.doWithFields(entityClass, field -> {
            if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
                fields.putIfAbsent(field.getName(), field);
        });
        List<DaoLogicGroupField> result = new ArrayList<>();
        Set<String> queryNames = new HashSet<>();
        fields.values().stream().sorted(Comparator.comparing(Field::getName)).forEach(field -> {
            GenDaoLogicGroup generation = field.getAnnotation(GenDaoLogicGroup.class);
            if (generation == null) return;
            DaoLogicGroup definition = definitions.get(generation.name().trim());
            if (definition == null)
                throw new IllegalArgumentException("GenDaoLogicGroup has no matching entity DaoLogicGroup: " + field);
            DaoLogicGroupField model = new DaoLogicGroupField(field, definition, generation);
            if (!queryNames.add(model.queryFieldName)
                    || (!model.queryFieldName.equals(model.sourceFieldName) && fields.containsKey(model.queryFieldName)))
                throw new IllegalArgumentException("Generated DAO logic field name conflicts: " + model.queryFieldName);
            result.add(model);
        });
        Set<String> protectedGroups = new HashSet<>();
        long orgScopeSources = result.stream()
                .filter(model -> model.scopeRole == GenDaoLogicGroup.ScopeRole.ORG)
                .map(model -> model.injectVarName).distinct().count();
        if (orgScopeSources > 1)
            throw new IllegalArgumentException("Participant organizations must share the same trusted scope source");
        result.stream().filter(model -> model.scopeRole != GenDaoLogicGroup.ScopeRole.NONE)
                .forEach(model -> protectedGroups.add(model.groupDefinition.name().trim()));
        for (Field field : fields.values()) {
            GenDaoLogicGroup marker = field.getAnnotation(GenDaoLogicGroup.class);
            if (marker != null && protectedGroups.contains(marker.name().trim())
                    && marker.scopeRole() == GenDaoLogicGroup.ScopeRole.NONE)
                throw new IllegalArgumentException("Untrusted condition in participant scope group: " + field);
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof GenDaoLogicGroup) continue;
                try {
                    Object group = annotation.annotationType().getMethod("group").invoke(annotation);
                    if (group instanceof String && protectedGroups.contains(((String) group).trim()))
                        throw new IllegalArgumentException("Extra condition in participant scope group: " + field);
                } catch (NoSuchMethodException ignored) {
                    // Other annotations do not contribute to a named DAO group.
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Cannot inspect participant scope annotation: " + field, e);
                }
            }
        }
        for (DaoLogicGroup group : definitions.values()) {
            if (protectedGroups.contains(group.parent().trim()))
                throw new IllegalArgumentException("Participant scope group cannot have a child group: " + group.name());
        }
        return List.copyOf(result);
    }

    public static Map<String, DaoLogicGroup> definitionsForType(Class<?> entityClass) {
        Map<String, DaoLogicGroup> definitions = new LinkedHashMap<>();
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            Set<String> namesOnType = new HashSet<>();
            for (DaoLogicGroup definition : type.getDeclaredAnnotationsByType(DaoLogicGroup.class)) {
                if (!StringUtils.hasText(definition.name()) || definition.minConditions() < 0)
                    throw new IllegalArgumentException("Invalid DAO logic group on entity: " + type);
                String groupName = definition.name().trim();
                if (!namesOnType.add(groupName))
                    throw new IllegalArgumentException("Duplicate DAO logic group on entity: " + groupName);
                if (groupName.equals(definition.parent().trim()))
                    throw new IllegalArgumentException("DAO logic group cannot be its own parent: " + groupName);
                if (definitions.putIfAbsent(groupName, definition) != null)
                    throw new IllegalArgumentException("Duplicate DAO logic group in entity hierarchy: " + groupName);
            }
        }
        for (String name : definitions.keySet()) {
            Set<String> path = new HashSet<>();
            String cursor = name;
            while (!cursor.isEmpty()) {
                if (!path.add(cursor))
                    throw new IllegalArgumentException("Recursive DAO logic group parent: " + cursor);
                DaoLogicGroup group = definitions.get(cursor);
                if (group == null)
                    throw new IllegalArgumentException("DAO logic group parent does not exist: " + cursor);
                cursor = group.parent().trim();
            }
        }
        return Collections.unmodifiableMap(definitions);
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        throw new IllegalArgumentException("Unsupported primitive DAO logic field: " + type);
    }

    private static String literal(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    public static String annotationSource(DaoLogicGroup group) {
        return "@DaoLogicGroup(name = " + literal(group.name().trim())
                + ", parent = " + literal(group.parent().trim())
                + ", logicOp = DaoLogicGroup.LogicOp." + group.logicOp().name()
                + ", condition = " + literal(group.condition())
                + ", minConditions = " + group.minConditions()
                + ", unless = " + literal(group.unless())
                + ", remark = " + literal(group.remark()) + ")";
    }

    public String getSourceFieldName() { return sourceFieldName; }
    public String getQueryFieldName() { return queryFieldName; }
    public String getQueryTypeName() { return queryTypeName; }
    public String getFilterAnnotationName() { return filterAnnotationName; }
    public String getGroupName() { return groupDefinition.name().trim(); }
    public String getGroupLiteral() { return literal(groupDefinition.name().trim()); }
    public String getInjectVarLiteral() { return literal(injectVarName); }
    public String getInjectOverrideLiteral() { return literal(injectOverride); }
    public String getInjectRequiredLiteral() { return literal(injectRequired); }
    public String getTitleLiteral() { return literal(title); }
    public GenDaoLogicGroup.ScopeRole getScopeRole() { return scopeRole; }
}
