package com.levin.commons.dao.annotation.stat;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 *分组统计注解
 *
 * 注意：目前所有的Having条件之间只支持逻辑与的关系
 *
 * 只有标量注解和GroupBy注解才支持Having操作
 *
 *一个字段可以有多个标量注解
 *
 * @author llw
 * @version 2.0.0
 */
public @interface GroupBy {

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";

    /**
     * 是否时必须的
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
     * 优先级
     * <p/>
     * 按数值从小到大排序
     *
     * @return
     */
    int order() default 0;


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
    String op() default "";

    /**
     * 可以使用函数
     * <p/>
     * <p/>
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default "";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "字段注解(语句组成: op + prefix + value + suffix)";

}
