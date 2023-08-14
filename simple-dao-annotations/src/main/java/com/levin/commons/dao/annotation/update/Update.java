package com.levin.commons.dao.annotation.update;

import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Func;
import com.levin.commons.dao.annotation.misc.Case;

import java.lang.annotation.*;

@Repeatable(Update.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 更新字段注解
 *
 * 支持增量更新
 *
 *
 * 语句的组成: value = op + prefix + ? + suffix
 * 如： name = to_date('',?)
 * op 可以是函数
 * prefix 可以是 (
 * suffix 可以是 )
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Update {

    /**
     *
     * 具体的变量替换，请参考 C 注解
     *
     *
     */

    /**
     * 操作
     *
     * @return
     */
    // Op op() default Op.Update;

    /**
     * 查询字段名称，默认为字段的属性名称
     * <p>
     * 对应数据库的字段名或是 Jpa 实体类的字段名
     *
     * @return
     */
    String value() default "";

    /**
     * 动态的 where 条件，通常用于乐观锁条件。
     * <p>
     * 可以支持SPEL_PREFIX，以"#!spel:"为前缀，表示是Spel表达式
     * <p>
     *
     * @return
     * @todo
     */
    String whereCondition() default "";

    /**
     * 是否增量更新
     * <p>
     * 对于数值形，增量更语句为 v = v  +  参数值
     * <p>
     * 对应字符串，增量更语句为 v = CONCAT(v  ,  参数值)
     * <p>
     * 对于时间，不支持，将抛出异常
     *
     * @return
     * @since 2.5.1
     */
    boolean incrementMode() default false;

    /**
     * 增量更新时，是否自动转换 NULL 值到 空字符串 或是 0
     * <p>
     * increment
     *
     * @return
     */
    boolean convertNullValueForIncrementMode() default true;

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
     * 是否过滤数组参数或是列表参数中的空值
     * <p>
     * 主要针对 In NotIn Between
     *
     * @return
     */
    boolean filterNullValue() default true;

    /**
     * 右操作数（参数） Case 选项
     * 当存在多个时，只取第一个条件成立的 Case
     * <p>
     * 注意该表达式比 paramFuncs 更早求取
     *
     * @return
     */
    Case[] paramCases() default {};

    /**
     * 针对参数的函数列表
     * <p>
     * 后面的函数嵌套前面的函数
     * <p>
     * 参数是指字段值或是子查询语句
     * <p>
     * 例如 func(:?)  把参数用函数包围
     * func(select name from user where id = :userId) 把子查询用函数包围
     *
     * @return
     */
    Func[] paramFuncs() default {};


    /**
     * 对整个表达式的包围前缀
     *
     * @return
     */
    String surroundPrefix() default "";


    /**
     * 字段归属的域，通常是表的别名
     *
     * @return
     */
    String domain() default "";

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
    String desc() default "语句表达式生成规则： surroundPrefix + op.gen( fieldFuncs( fieldCases(domain.fieldName) ), paramFuncs( fieldCases([ paramExpr(优先) or 参数占位符 ])) ) +  surroundSuffix";

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
        Update[] value();
    }
}
