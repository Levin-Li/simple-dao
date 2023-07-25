package com.levin.commons.dao.codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

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
        return fieldModels != null
                && !fieldModels.isEmpty()
                && fieldModels.stream().anyMatch(fm -> fm.name.contains(attrName));
    }

    public String attrName(String attrName, String prefix, String suffix) {
        return hasAttr(attrName) ? (prefix + attrName + suffix) : "";
    }

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
