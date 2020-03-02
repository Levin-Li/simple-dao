package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited


/**
 *
 * 正常情况下不建议使用
 *
 * @author llw
 * @version 2.0.0
 */

//@Deprecated
public @interface Where {

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * @return
     */
    String value() default "";

    /**
     * 是否是必须的，也就是condition必须返回TRUE
     * 条件必须成立，否则会抛出异常
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
    String condition() default "";

    /**
     * 操作符
     *
     * @return
     */
    String op() default "";

    /**
     * 在构建语句时，是否使用字段值
     * <p/>
     * 如比较特别的情况
     * <p/>
     * 如： to_date(?,t.update_date) from tab t
     * 参数为：yyyy-MM-dd
     * <p/>
     * 注解表达为： op = to_date
     * prefix = (?,
     * suffix = )
     * <p/>
     *
     * @return
     */
    boolean useVarValue() default false;

    /**
     * 条件语句，只支持命名参数
     *
     * <p>
     * 可以使用命名参数，命名参数的使用有限制，如：name = :paramName
     * <p>
     * <p>
     * 可以使用变量替换，更为通用，如： name = ${:paramName}
     * 变量替换使用 :？作为占位符，不会存在参数顺序问题
     *
     *
     * @return
     */
    String statement() default "";


    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default "";


    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "语句组成：op + prefix + statement + suffix";

}
