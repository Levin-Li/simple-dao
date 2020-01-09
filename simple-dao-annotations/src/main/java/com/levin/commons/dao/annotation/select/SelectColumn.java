package com.levin.commons.dao.annotation.select;


import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 选择字段注解
 *
 *
 * 语句的组成:   op + prefix + value + suffix
 * 如：  to_date('yyyy',fieldName)
 *
 * op 可以是函数，如to_date
 * prefix 可以是 (
 * suffix 可以是 )
 *
 * 默认情况下，注解不会使用字段值，需要指定 useVarValue = true
 *
 * @author llw
 * @version 2.0.0
 */
public @interface SelectColumn {

    /**
     * 查询字段名称，默认为字段的属性名称
     * 如果是子查询，则当成别名
     *
     * @return
     */
    String value() default "";


    /**
     * 输出时的列名
     *
     *
     * @return
     */
    String outputColumnName() default "";


    /**
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
     * 在构建语句时，是否使用字段值
     * <p/>
     * 如比较特别的情况
     * <p/>
     * 如：select to_date(?,t.update_date) from tab t
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
     * 是否增加别名前缀
     *
     * @return
     */
    boolean isAppendAliasPrefix() default false;

    /**
     * 当参数或是字段值为空时，是否忽略这个列
     *
     * @return
     */
    boolean ignoreNullValue() default false;

    /**
     * 操作符
     *
     * @return
     */
    String op() default "";

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
    String desc() default "选择字段注解(语句组成: op + prefix + value + suffix)";

}
