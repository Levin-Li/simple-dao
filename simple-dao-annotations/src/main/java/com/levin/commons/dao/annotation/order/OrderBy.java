package com.levin.commons.dao.annotation.order;

import com.levin.commons.dao.annotation.misc.Case;

import java.lang.annotation.*;

@Repeatable(OrderBy.List.class)
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
 * @author llw
 * @version 2.0.0
 */
public @interface OrderBy {

    enum Type {
        Asc,
        Desc
    }

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";


    /**
     * case 支持
     *
     * @return
     */
    Case[] cases() default {};

    /**
     * 字段归属的域，通常是表的别名
     *
     * @return
     */
    String domain() default "";

    /**
     * 是否是必须的
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
     * 排序优先级
     * <p/>
     * 升序排序，按数值从小到大排序
     * <p/>
     *
     * @return
     */
    int order() default 0;


    /**
     * 操作符 asc
     * <p/>
     * desc or asc
     *
     * @return
     */
    Type type() default Type.Desc;


    /**
     * 是否使用别名做为排序关键字
     *
     * @return
     */
    boolean useAlias() default false;

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "";

    /**
     * 列表
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface List {
        /**
         * 是否是必须的，如果条件不匹配，但又是必须的，将抛出异常
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
         * 注解列表
         *
         * @return
         */
        OrderBy[] value();
    }
}
