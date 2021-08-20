package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.annotation.Annotation;

@Data
@Accessors(chain = true)
public class BaseModel implements Serializable {

    /**
     * 原注解
     */
    Annotation annotation;

    /**
     *
     */
    PrimitiveValue primitiveValue;

    /**
     * 操作
     */
    Op op;

    /**
     * 字段归属的域，通常是表的别名
     *
     * @return
     */
    String domain;

    /**
     * 通常是字段名
     */
    String value;

    /**
     *
     */
    boolean required;

    /**
     * 表达式，默认为SPEL
     * <p>
     * <p>
     * 如果用 groovy:  做为前缀则是 groovy脚本
     * <p>
     *
     *
     * <p>
     * <p>
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition;

    /**
     *
     */
    String remark;


    TargetModel targetModel;


    @Data
    @Builder
    @Accessors(chain = true, fluent = true)
    public static class TargetModel {

        Object rootBean;

        Object fieldOrMethod;

        Annotation[] varAnnotations;

        String name;

        Class<?> varType;

        Object value;

    }

}
