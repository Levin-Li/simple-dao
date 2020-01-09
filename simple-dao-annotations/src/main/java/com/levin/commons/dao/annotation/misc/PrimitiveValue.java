package com.levin.commons.dao.annotation.misc;

import java.lang.annotation.*;

@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 特别注解
 *
 *
 * 强制注明一个属性是原子类型的属性，不管字段是不是真实的原子对象
 *
 *用于像jpa之类的复合组件的查询
 *
 * @author llw
 * @version 2.0.0
 */
public @interface PrimitiveValue {
    /**
     * 描述信息
     *
     * @return
     */
    String value() default "";

    /**
     * @return
     */
    boolean require() default false;


    /**
     * @return
     */
    String desc() default "";
}
