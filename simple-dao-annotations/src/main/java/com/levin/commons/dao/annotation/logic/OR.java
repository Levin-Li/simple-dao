package com.levin.commons.dao.annotation.logic;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 或关系分组
 *
 * 在这个分组中，所有的条件之间是或的关系
 *
 *
 * 最终将产生语句示例：(A=? OR B=? OR C=?)
 *
 * @author llw
 * @version 2.0.0
 */
public @interface OR {

    /**
     * 查询字段名称，默认为字段的属性名称
     * 暂元作用
     *
     * @return
     */
    String value() default "";

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
     * 操作符
     *
     * @return
     */
    String op() default " OR ";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default " ( ";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default " ) ";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "或关系组";

}
