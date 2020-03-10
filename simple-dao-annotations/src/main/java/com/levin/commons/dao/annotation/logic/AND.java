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
     * 在统一个字段上是否自动闭合逻辑组
     *
     * @return
     */
    boolean autoClose() default false;

    /**
     *
     * 是否是必须
     *
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
     * 描述信息
     *
     * @return
     */
    String desc() default "与关系组";
}
