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
 * @since 2.2.27
 */

@Repeatable(CtxVar.List.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CtxVar {

    /**
     * 求值表达式
     * <p>
     * 默认为 Spel 表达式 {@link ExpressionType} SPEL_PREFIX
     * <p>
     * 为空 表示取当前字段值
     *
     * @return
     * @See ExpressionType {@link ExpressionType}
     */
    String value() default "";

    /**
     * 环境中的变量名称
     * <p>
     * 默认当前字段名称
     *
     * @return
     */
    String varName() default "";

    /**
     * 是否强制覆盖旧的环境变量值
     *
     * @return
     */
    boolean forceOverride() default true;

    /**
     * 注解生效条件
     * <p>
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

    /**
     * 列表
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface List {
        /**
         * 注解列表
         *
         * @return
         */
        CtxVar[] value();
    }
}
