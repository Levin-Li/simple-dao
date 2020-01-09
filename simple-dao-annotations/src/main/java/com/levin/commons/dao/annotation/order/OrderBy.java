package com.levin.commons.dao.annotation.order;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
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
     * @return
     */
    boolean require() default false;

    /**
     * 是否增加别名前缀
     *
     * @return
     */
    boolean isAppendAliasPrefix() default true;

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
     * 按数值从小到大排序
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
     * 描述信息
     *
     * @return
     */
    String desc() default "";

}
