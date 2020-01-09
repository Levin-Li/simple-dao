package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

//在选项中
/**
 * 暂时不支持
 *
 * @author llw
 * @version 2.0.0
 */
public @interface In {

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";

    /**
     * @return
     */
    boolean require() default false;


    /**
     * 是否过滤空值
     *
     * @return
     */
    boolean filterNullValue() default true;

    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default "#_val != null and #_val != ''";

    /**
     * 操作符
     *
     * @return
     */
    String op() default "IN";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "(";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default ")";

    /**
     * 子查询语句
     * <p/>
     * 如果子查询语句有配置，将会被优先使用，被注解的字段将做为参数
     * 被注解的字段，只能数组，列表，或是Map,如果都不是，将被做为一个参数，否则是多个参数
     * 注意：语句中只能使用 ？作参数，或是命名参数，不能使用?1 这种形式的参数
     *
     * @return
     */
    String subQuery() default "";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "";
}
