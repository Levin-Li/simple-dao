package com.levin.commons.dao.annotation;

import com.levin.commons.annotation.GenNameConstant;

import java.lang.annotation.*;

/**
 * 单参数的函数
 *
 * @since 2.1.0
 */

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@GenNameConstant
public @interface Func {

    /**
     * 原表达式
     */
    String ORIGIN_EXPR = "$$";


    /**
     * 条件
     * SPEL 表达式
     *
     * @return
     */
    String condition() default "";

    /**
     * 函数名称
     *
     * @return
     */
    String value() default "";


    /**
     * 对操作数的包围前缀
     * <p>
     * 支持参数表达式
     *
     * @return
     */
    String prefix() default "(";

    /**
     * 函数的参数
     * <p>
     * 例如日期函数  'yyyyMM' , $$
     *
     * @return
     */
    String[] params() default {ORIGIN_EXPR};


    /**
     * 参数分隔符
     * 默认式逗号
     *
     * @return
     */
    String paramDelimiter() default ",";


    /**
     * 对操作数的包围后缀
     * <p>
     * 支持参数表达式
     * <p>
     * 如日期函数：toDate( :?,:format)
     * <p>
     * 则后缀为： ,:format)
     *
     * @return
     */
    String suffix() default ")";

    /**
     * 说明
     *
     * @return
     */
    String desc() default "函数表达式生成规则：value + prefix + params + suffix";

}
