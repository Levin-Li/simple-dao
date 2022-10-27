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

    @SneakyThrows
    public boolean isType(String className) {
        return Class.forName(className).isAssignableFrom(entityType);
    }

}
