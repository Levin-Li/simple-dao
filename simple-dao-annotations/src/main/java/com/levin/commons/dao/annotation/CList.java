package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * 语句注解集合
 *
 * @author llw
 * @@since 2.1.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CList {

    /**
     * 注解集合
     *
     * @return
     */
    C[] value() default {};

    /**
     * 是否迭代
     * <p>
     * 根据字段值类型，当为数组或是集合对象时，自动展开，迭代注解
     *
     * @return
     */
    boolean isIterative() default true;

    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default "";

}
