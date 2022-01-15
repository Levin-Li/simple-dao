package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * 语句注解集合
 * <p>
 * 本组件主要解决同一个字段，需要出现多个相同的注解的问题
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


    /**
     * 是否是必须的，如果条件不匹配，但又是必须的，将抛出异常
     *
     * @return
     */
    boolean require() default false;

}
