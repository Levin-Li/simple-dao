package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * dao 查询结果拷贝的 DTO 对象
 * <p>
 * 该注解主要用于 DTO 类中的字段
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DeepCopy {

    /**
     * 拷贝的属性
     *
     * @return
     */
    String value();


    /**
     * 拷贝深度
     *
     * @return
     */
    int maxCopyDeep() default 3;

    /**
     * 忽略的属性
     * <p>
     * a.b.c.name* *号表示忽略以什么开头的属性
     * a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     * a.b.c.{com.User}    大括号表示忽略User类型属性
     *
     * @return
     */
    String[] ignoreProperties() default {};

}
