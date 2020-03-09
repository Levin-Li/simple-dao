package com.levin.commons.dao.annotation.update;

import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Func;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 更新字段注解
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
     * 是否是having 操作
     * <p>
     * 只针对查询有效
     *
     * @return
     */
//    boolean having() default false;


    /**
     * 是否用 NOT () 包围
     *
     * @return
     */
//    boolean not() default false;


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
    String condition() default C.NOT_NULL;

    /**
     * 是否过滤数组参数或是列表参数中的空值
     * <p>
     * 主要针对 In NotIn Between
     *
     * @return
     */
    boolean filterNullValue() default true;


    /**
     * 针对字段函数列表
     * 后面的函数嵌套前面的函数
     * <p>
     * func3(func2(func1(t.field)
     *
     * <p>
     * <p>
     * 如果是更新字段则忽略
     *
     * @return
     */
    Func[] fieldFuncs() default {};


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
    String subQuery() default "";


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
    String desc() default "语句表达式生成规则： surroundPrefix + op.gen( func(fieldName), func([subQuery or fieldValue])) +  surroundSuffix ";

}
