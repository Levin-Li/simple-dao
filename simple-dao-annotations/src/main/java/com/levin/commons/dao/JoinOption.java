package com.levin.commons.dao;

import com.levin.commons.dao.annotation.misc.Fetch;

import java.lang.annotation.*;

/**
 *
 * 连接注解
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JoinOption {

    /**
     * 连接类型
     *
     * @return
     */
    Fetch.JoinType type() default Fetch.JoinType.Left;

    /**
     * 默认查询的目标实体类
     *
     * @return
     */
    Class entityClass() default Void.class;

    /**
     * 表名或是语句表达式
     *
     * @return
     */
    String tableOrStatement() default "";

    /**
     * 别名，必须，并且别名，不允许重名
     * <p>
     *
     * @return
     */
    String alias();

    /**
     * 连接的列名或是字段名
     *
     * @return
     */
    String joinColumn() default "";

    /**
     * 连接的目标的别名
     * <p>
     * 默认是 @TargetOption注解中的别名
     *
     * @return
     */
    String joinTargetAlias() default "";

    /**
     * 连接目标的列名或是字段名
     * <p>
     * <p>
     * 如果是表，必须指定
     *
     * @return
     */
    String joinTargetColumn() default "";

}
