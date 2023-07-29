package com.levin.commons.dao.codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(of = "name")
@ToString()
@Accessors(chain = true)
public class ClassModel {

    Class entityType;

    private String name;

    //类的短名称
    private String typeName;

    List<FieldModel> fieldModels;

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

    public String attrName(String attrName, String prefix, String suffix) {
        return hasAttr(attrName) ? (prefix + attrName + suffix) : "";
    }

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
