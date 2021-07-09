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
     * 排序方式，可以用字段隔开
     *
     * @return
     */
    String value() default "";


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
     * 按数值从大到小排序
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

}
