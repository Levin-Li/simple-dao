package com.levin.commons.dao.codegen.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.alibaba.fastjson2.JSON;
import com.levin.commons.rbac.RbacDataAttrGroupMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(of = "name")
@ToString
@Accessors(chain = true)
public class ClassModel {

    @Data
    @Accessors(chain = true)
    public static class UniqueKeyModel {
        private List<String> propertyNames;

        public String getId() {
            return String.join("|", propertyNames);
        }

        public String getMethodSuffix() {
            return propertyNames.stream().map(name -> Character.toUpperCase(name.charAt(0)) + name.substring(1)).collect(Collectors.joining("And"));
        }

        public String getCacheNameSuffix() {
            return getMethodSuffix();
        }
    }

    Class<?> entityType = Void.class;

    private final RbacDataAttrGroupMetadata dataAttrGroupMetadata;

    String name;

    //类的短名称
    String typeName;

    private long serialNo = 1;

    private final Set<String> imports = new LinkedHashSet<>();
    private final Set<String> annotations = new LinkedHashSet<>();

    private final Set<String> implementsList = new LinkedHashSet<>();

    List<FieldModel> fieldModels = Collections.emptyList();

    List<UniqueKeyModel> uniqueKeyModels = Collections.emptyList();

    public ClassModel(Class<?> entityType) {

        this.entityType = entityType;
        this.dataAttrGroupMetadata = RbacDataAttrGroupMetadata.forType(entityType);

        this.imports.add("java.time.*");

        for (Annotation annotation : this.entityType.getAnnotations()) {
            //加入Json的注解
            if (annotation.annotationType().getPackage()
                    .equals(JsonIgnore.class.getPackage())) {
                imports.add(annotation.annotationType().getCanonicalName());
                annotations.add("@" + annotation.annotationType().getSimpleName());
            }
        }
    }

    /** Emits normalized source groups, independent of source availability or copied annotation filters. */
    public List<String> rbacAnnotations(String attribute) {
        return groupsForAttribute(attribute).stream()
                .map(group -> rbacAnnotation(group, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    private List<RbacDataAttrGroupMetadata.Group> groupsForAttribute(String attribute) {
        Map<String, RbacDataAttrGroupMetadata.Group> groups = new LinkedHashMap<>();
        RbacDataAttrGroupMetadata metadata = dataAttrGroupMetadata;
        metadata.getGroupsForAttribute(attribute).forEach(group -> groups.put(group.getName(), group));
        // The generated display name exposes the same identity as its protected source ID.
        if (Arrays.asList("tenantName", "orgName", "ownerName").contains(attribute)) {
            String source = attribute.substring(0, attribute.length() - 4) + "Id";
            metadata.getGroupsForAttribute(source).forEach(group -> groups.put(group.getName(), group));
        }
        return new ArrayList<>(groups.values());
    }

    /** Info fields moved into common base classes still need model-specific group declarations. */
    public List<String> rbacInheritedAnnotations(boolean info) {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        Map<String, RbacDataAttrGroupMetadata.Group> groups = new LinkedHashMap<>();
        List<String> inherited = new ArrayList<>();
        if (com.levin.commons.dao.domain.MultiTenantObject.class.isAssignableFrom(entityType))
            inherited.addAll(info ? Arrays.asList("tenantId", "tenantName") : Collections.singletonList("tenantId"));
        if (com.levin.commons.dao.domain.OrganizedObject.class.isAssignableFrom(entityType))
            inherited.addAll(info ? Arrays.asList("orgId", "orgName") : Collections.singletonList("orgId"));
        if (com.levin.commons.dao.domain.PersonalObject.class.isAssignableFrom(entityType))
            inherited.addAll(info ? Arrays.asList("ownerId", "ownerName") : Collections.singletonList("ownerId"));
        if (!info && com.levin.commons.dao.domain.DomainObject.class.isAssignableFrom(entityType))
            inherited.add("domainId");
        for (String attribute : inherited) {
            String source = attribute.endsWith("Name") ? attribute.substring(0, attribute.length() - 4) + "Id" : attribute;
            if (ReflectionUtils.findField(entityType, source) == null) continue;
            if (fieldModels.stream().anyMatch(field -> attribute.equals(field.getName()))) continue;
            for (RbacDataAttrGroupMetadata.Group group : groupsForAttribute(attribute)) {
                groups.put(group.getName(), group);
                attributes.computeIfAbsent(group.getName(), key -> new ArrayList<>()).add(attribute);
            }
        }
        attributes.forEach((name, attrs) -> result.add(rbacAnnotation(groups.get(name), attrs)));
        return result;
    }

    private static String rbacAnnotation(RbacDataAttrGroupMetadata.Group group, List<String> attributes) {
        String attrs = attributes.isEmpty() ? "" : ", attrList = {" + attributes.stream()
                .map(ClassModel::javaString).collect(Collectors.joining(", ")) + "}";
        return "@com.levin.commons.rbac.RbacDataAttrGroup(name = " + javaString(group.getName())
                + attrs + ", confidentialLevel = " + group.getConfidentialLevel()
                + ", remark = " + javaString(group.getRemark()) + ")";
    }

    private static String javaString(String value) {
        return JSON.toJSONString(value == null ? "" : value);
    }

    public boolean hasAttr(String attrName) {
        return StringUtils.hasText(attrName)
                && fieldModels != null
                && !fieldModels.isEmpty()
                && fieldModels.stream().anyMatch(fm -> fm.name.contains(attrName));
    }

    public boolean isDefaultUpdateTime(String attrName) {
        return StringUtils.hasText(attrName) && (
                attrName.endsWith("lastUpdateTime")
                        || attrName.endsWith("updateTime")
                        || attrName.endsWith("modifyTime")
        );
    }

    public boolean isDefaultCreateTime(String attrName) {
        return StringUtils.hasText(attrName) && (
                attrName.endsWith("createTime")
                        || attrName.endsWith("addTime")
                        || attrName.endsWith("occurTime")
        );
    }

    /**
     * 查找第一个属性
     *
     * @param attrNames
     * @return
     */
    public String findFirstAttr(String... attrNames) {
        if (attrNames == null || attrNames.length == 0) {
            return null;
        }
        return Stream.of(attrNames).filter(this::hasAttr).findFirst().orElse(null);
    }

    /**
     * 查找第一个属性
     *
     * @param attrNames
     * @return
     */
    public FieldModel findFirstField(String... attrNames) {

        String firstAttr = findFirstAttr(attrNames);

        if (!StringUtils.hasText(firstAttr)) {
            return null;
        }

        return fieldModels.stream()
                .filter(f -> firstAttr.equals(f.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找字段描述
     *
     * @param attrNames
     * @return
     */
    public List<FieldModel> findFields(String... attrNames) {

        if (attrNames == null || attrNames.length == 0) {
            return Collections.emptyList();
        }

        final List<String> attrList = Stream.of(attrNames).filter(StringUtils::hasText).collect(Collectors.toList());

        if (attrList.isEmpty()) {
            return Collections.emptyList();
        }

        return fieldModels.stream()
                .filter(f -> attrList.contains(f.getName()))
                .collect(Collectors.toList());
    }

    public List<FieldModel> findFields(Collection<String> attrNames) {
        if (attrNames == null || attrNames.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, FieldModel> fieldsByName = fieldModels.stream()
                .collect(Collectors.toMap(FieldModel::getName, field -> field, (left, right) -> left));
        return attrNames.stream().map(fieldsByName::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public long getNextOrderNum() {
        return serialNo++ * 10;
    }

    @SneakyThrows
    public boolean hasAnno(String annoType) {
        return entityType.isAnnotationPresent((Class<? extends Annotation>) Class.forName(annoType));
    }


    public boolean hasOptimisticLockField() {
        return ReflectionUtils.findField(entityType, "optimisticLock") != null;
    }


    public String attrName(String attrName, String prefix, String suffix) {
        return hasAttr(attrName) ? (prefix + attrName + suffix) : "";
    }

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
