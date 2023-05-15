package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * <p>StartsWith class.</p>
 *
 * @author llw
 * @version 2.0.0
 */
@Repeatable(Where.List.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Where {

    /**
     *
     * 具体的变量替换，请参考 C 注解
     *
     */

    /**
     * 操作
     *
     * @return
     */
    Op op() default Op.Expr;

    /**
     * 字段归属的域，通常是表的别名
     *
     * @return
     */
    String domain() default "";
//
//
//    /**
//     * 查询字段名称，默认为字段的属性名称
//     * <p>
//     * 对应数据库的字段名或是 Jpa 实体类的字段名
//     *
//     * @return
//     */
//    String value() default "";
//

    /**
     * 子查询表达式
     * <p>
     * <p/>
     * 如果子查询语句有配置，将会使被注解的字段值不会被做为语句生成部分
     * <p>
     * <p>
     * 被注解的字段，
     * 如果是是数组，列表，如果
     *
     * @return
     */
    String paramExpr() default "";

    /**
     * 是否是having 操作
     * <p>
     * 只针对查询有效
     *
     * @return
     */
    boolean having() default false;


    /**
     * 是否用 NOT () 包围
     *
     * @return
     */
    boolean not() default false;


    /**
     * 是否是必须的，如果条件不匹配，但又是必须的，将抛出异常
     *
     * @return
     */
    boolean require() default false;


    /**
     * 表达式，默认为SPEL
     * <p>
     * <p>
     * 如果用 groovy:  做为前缀则是 groovy脚本
     * <p>
     *
     *
     * <p>
     * <p>
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default C.VALUE_NOT_EMPTY;


    /**
     * 对整个表达式的包围前缀
     *
     * @return
     */
    String surroundPrefix() default "";

    /**
     * 对整个表达式的包围后缀
     *
     * @return
     */
    String surroundSuffix() default "";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "语句表达式生成规则： surroundPrefix + op.gen( fieldFuncs( fieldCases(domain.fieldName) ), paramFuncs( paramCases([ paramExpr(优先) or 参数占位符 ])) ) +  surroundSuffix";


    /**
     * 列表
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface List {
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
        String condition() default "";

        /**
         * 注解列表
         *
         * @return
         */
        Where[] value();
    }

}
