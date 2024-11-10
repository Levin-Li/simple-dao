package com.levin.commons.dao;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.annotation.misc.Fetch;

import java.lang.annotation.*;

/**
 * 连接注解
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@GenNameConstant
public @interface JoinOption {

    /**
     * 连接类型
     *
     * @return
     */
    Fetch.JoinType type() default Fetch.JoinType.Left;

    /**
     * 实体类
     * <p>
     * 对应表名
     * 可以填 Void.class
     * <p>
     * 优先于 tableOrStatement 属性
     *
     * @return
     */
    Class<?> entityClass();

    /**
     * 表名或是语句表达式
     * <p>
     * 一般不建议使用，使用原生查询时，也可以通过实体名获取表名
     *
     * @return
     */
    @Deprecated
    String tableOrStatement() default "";

    /**
     * 别名，必须，并且别名，不允许重名
     * <p>
     * 自己的别名
     *
     * @return
     */
    String alias() default "";

    /**
     * on 的条件表达式，可以是多个连接条件, 如：Left JOIN table2 ON table1.id = table2.id AND table1.name = table2.name
     * <p>
     * 如果手动指定了 join on 表达式，则不自动生成on表达式
     *
     * @return
     */
    String onExpr() default "";

    /**
     * 本表的字段名，连接的列名或是字段名
     * <p>
     * 默认为主键字段名
     * <p>
     *
     * @return
     */
    String joinColumn() default "";

    /**
     * 连接的目标的别名
     * <p>
     * 默认是主表的别名 @TargetOption注解中的别名
     *
     * @return
     */
    String joinTargetAlias() default "";

    /**
     * 连接目标表的列名或是字段名，注意不是本注解 entityClass 中的字段名，而是目标表的字段名
     * <p>
     * <p>
     * 如果是表，必须指定
     *
     * @return
     */
    String joinTargetColumn() default "";

}
