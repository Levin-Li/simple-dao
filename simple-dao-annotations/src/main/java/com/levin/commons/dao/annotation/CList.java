package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * 语句注解集合
 * <p>
 * 本组件主要解决同一个字段，需要出现多个相同的注解的问题
 * <p>
 * <p>
 * 使用C.List代替
 *
 * @author llw
 * @@since 2.1.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Deprecated//使用C.List代替
public @interface CList {

    /**
     * 注解集合
     *
     * @return
     */
    C[] value();

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
