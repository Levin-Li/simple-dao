package com.levin.commons.dao.annotation.stat;

import java.lang.annotation.*;

/**
 * <p>Min class.</p>
 *
 * @author llw
 * @version 2.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Min {

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
    String condition() default "";

    /**
     * Having 操作符
     * <p/>
     * 所有的group by 条件都可以做为having条件
     * <p/>
     * 注意：目前所有的Having条件之间只支持逻辑与的关系
     *
     * @return
     */
    String havingOp() default "";

    /**
     * 操作符
     *
     * @return
     */
    String op() default " MIN";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "(";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default ") ";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "字段注解(语句组成: op + prefix + value + suffix)";
}
