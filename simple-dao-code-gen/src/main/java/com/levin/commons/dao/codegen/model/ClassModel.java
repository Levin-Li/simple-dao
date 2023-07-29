package com.levin.commons.dao.codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(of = "name")
@ToString
@Accessors(chain = true)
public class ClassModel {

    Class entityType = Void.class;

    String name;

    //类的短名称
    String typeName;

    List<FieldModel> fieldModels = Collections.emptyList();

    public boolean hasAttr(String attrName) {
        return StringUtils.hasText(attrName)
                && fieldModels != null
                && !fieldModels.isEmpty()
                && fieldModels.stream().anyMatch(fm -> fm.name.contains(attrName));
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

    public String attrName(String attrName, String prefix, String suffix) {
        return hasAttr(attrName) ? (prefix + attrName + suffix) : "";
    }

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
