package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * <p>NotLike class.</p>
 *
 * @author llw
 * @version 2.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface C {

    /**
     * 不是 NUll 对象 ，也不是空字符串
     */
    String NOT_NULL = "#_val != null and (!(#_val instanceof T(CharSequence)) ||  #_val.trim().length() > 0)";


    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";


    /**
     * 操作
     *
     * @return
     */
    Op op();


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
    String condition() default NOT_NULL;


    /**
     *
     * 对整个表达式的包围前缀
     *
     * @return
     */
    String aroundPrefix() default "";

    /**
     * 子查询语句
     * <p/>
     * 如果子查询语句有配置，将会被优先使用，被注解的字段将做为参数
     * 被注解的字段，只能数组，列表，或是Map,如果都不是，将被做为一个参数，否则是多个参数
     * 注意：语句中只能使用 ？作参数，或是命名参数，不能使用?1 这种形式的参数
     * <p>
     * 支持字符串 String.format()
     * <p>
     * ${:sqlParamName}  --> SQL 语句的参数占位符
     *
     * @return
     */
    String subQuery() default "";

    /**
     * 子查询语句字符串参数替换
     *
     * @return
     */
    String[] subQueryFormatArgs() default {};


    /**
     * 对整个表达式的包围后缀
     *
     * @return
     */
    String  aroundSuffix() default "";

    /**
     * 描述信息
     *
     *
     * @return
     */
    String desc() default "";

}
