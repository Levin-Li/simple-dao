package com.levin.commons.dao.annotation.logic;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 与关系分组
 *
 * 关系组都可以无限嵌套
 *
 * 在这个分组中，所有的条件之间是与的关系
 *
 * 最终将产生语句示例：(A=? AND B=? AND C=?)
 *
 * @author llw
 * @version 2.0.0
 */
public @interface AND {

    /**
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
    String op() default " AND ";

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
    String desc() default "与关系组";
}
