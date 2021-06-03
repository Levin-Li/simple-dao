package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 上下文变量注解
 *
 * <p>
 * 上下文通常是一次查询或是更新的上下文
 * <p>
 * <p>
 * 字段值  -->  上下文变量
 * 上下文变量  -->  字段值
 * <p>
 * <p>
 * 把当前字段值保存到上下文变量，或是把上下文变量注入到字段
 *
 *
 * <p>
 *
 * @author levin li
 * @since 2.2.28
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CtxVar {

    /**
     * 变量名字
     * 默认为字段名
     * <p>
     * 或是 Spel 表达式 {@link ExpressionType} SPEL_PREFIX
     *
     * @return
     * @See ExpressionType {@link ExpressionType}
     */
    String value() default "";

    /**
     * 是否是注入，上下文变量  -->  字段值
     * <p>
     * false 表示是 字段值  -->  上下文变量
     *
     * @return
     */
    boolean inject() default false;


    /**
     * 是否强制覆盖
     * <p>
     * 默认强制覆盖
     *
     * @return
     */
    boolean forceOverride() default true;

    /**
     * 注解生效条件
     * SPEL 表达式
     * <p>
     * 默认没有条件
     *
     * @return
     */
    String condition() default "";

    /**
     * 备注
     *
     * @return
     */
    String remark() default "";

}
