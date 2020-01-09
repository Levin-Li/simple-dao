package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

//在选项中
/**
 *
 *
 * 如果少于2个参数，语句将自动转换为大于等于
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Between {

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";

    /**
     * 是否是必须的
     *
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
    String op() default "AND";

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
     * 描述信息
     *
     * @return
     */
    String desc() default "";
}
