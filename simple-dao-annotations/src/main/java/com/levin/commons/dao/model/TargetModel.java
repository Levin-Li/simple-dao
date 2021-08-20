package com.levin.commons.dao.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.annotation.Annotation;

@Data
@Builder
@Accessors(chain = true, fluent = true)
public class TargetModel {

    /**
     * 宿主对象
     */
    Object rootBean;

    /**
     * 字段或是方法的实例
     */
    Object fieldOrMethod;

    /**
     * 方法或是变量的所有注解
     */
    Annotation[] varAnnotations;

    /**
     * 变量名称
     */
    String name;

    /**
     * 定义的值类型
     * 对于方法，是返回值类型
     * 对于字段，是字段类型
     */
    Class<?> type;

    /**
     * 变量值
     */
    Object value;

}
