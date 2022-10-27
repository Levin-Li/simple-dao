package com.levin.commons.dao.annotation.order;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * OrderBy注解
 *
 * 强制排序
 *
 * 使用 OrderBy.List 代替
 *
 * @author llw
 * @version 2.0.0
 */
@Deprecated
public @interface OrderByList {

    /**
     * Order By List
     *
     * @return
     */
    OrderBy[] value();

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
