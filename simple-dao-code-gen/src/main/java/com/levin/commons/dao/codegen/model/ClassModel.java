package com.levin.commons.dao.codegen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
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

    Class<?> entityType = Void.class;

    String name;

    //类的短名称
    String typeName;

    private long serialNo = 1;

    private final Set<String> imports = new LinkedHashSet<>();
    private final Set<String> annotations = new HashSet<>();

    private final Set<String> implementsList = new HashSet<>();

    List<FieldModel> fieldModels = Collections.emptyList();

    public ClassModel(Class<?> entityType) {

        this.entityType = entityType;

        for (Annotation annotation : this.entityType.getAnnotations()) {
            //加入Json的注解
            if (annotation.annotationType().getPackage()
                    .equals(JsonIgnore.class.getPackage())) {
                imports.add(annotation.getClass().getName());
                annotations.add("@" + annotation.annotationType().getSimpleName());
            }
        }
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


    public long getNextOrderNum() {
        return serialNo * 100;
    }

    @SneakyThrows
    public boolean hasAnno(String annoType) {
        return entityType.isAnnotationPresent((Class<? extends Annotation>) Class.forName(annoType));
    }

    public String attrName(String attrName, String prefix, String suffix) {
        return hasAttr(attrName) ? (prefix + attrName + suffix) : "";
    }

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
