package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * <p>NotLike class.</p>
 *
 * @author llw
 * @version 2.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NotLike {

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";

    /**
     * @return
     */
    boolean require() default false;

    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default "#_val != null and #_val != ''";

    /**
     * 操作符
     *
     * @return
     */
    String op() default "NOT LIKE";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "%";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default "%";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "";
}
