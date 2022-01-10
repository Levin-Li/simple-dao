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
     * @return
     */
    C[] value() default {};

    /**
     * 是否迭代
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
