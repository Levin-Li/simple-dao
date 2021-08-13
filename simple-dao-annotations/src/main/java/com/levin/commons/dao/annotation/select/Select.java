package com.levin.commons.dao.annotation.select;


import com.levin.commons.dao.annotation.Func;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.misc.Case;
import com.levin.commons.dao.annotation.order.OrderBy;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
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
public @interface Select {


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
//    Op op() default Op.Select;


    /**
     * 强行设置字段表达式
     *
     * @return
     */
    String fieldExpr() default "";

    /**
     * 查询字段名称，默认为字段的属性名称
     * <p>
     * 对应数据库的字段名或是 Jpa 实体类的字段名
     *
     * @return
     */
    String value() default "";

    /**
     * 统计字段的排序
     *
     * @return
     */
    OrderBy[] orderBy() default {};

    /**
     * aving 操作
     * <p>
     *
     * @return
     */
    Op havingOp() default Op.None;


    /**
     * where 条件 是否用 NOT () 包围
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
    String condition() default "";


    /**
     * 是否是唯一值 DISTINCT
     */
    boolean distinct() default false;

    /**
     * 是否过滤数组参数或是列表参数中的空值
     * <p>
     * 主要针对 In NotIn Between
     *
     * @return
     */
    boolean filterNullValue() default true;


    /**
     * 左操作数（字段） Case 选项
     * 当存在多个时，只取第一个条件成立的 Case
     * <p>
     * 注意该表达式比 fieldFuncs 更早求取
     *
     * @return
     */
    Case[] fieldCases() default {};


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
//    Func[] paramFuncs() default {};


    /**
     * 对整个表达式的包围前缀
     *
     * @return
     */
    String surroundPrefix() default "";


    /**
     * 字段归属的域，通常是表的别名
     *
     * 如果为 "NULL" 值，则忽略这个值
     *
     * @return
     */
    String domain() default "";


    /**
     * 对整个表达式的包围后缀
     *
     * @return
     */
    String surroundSuffix() default "";


    /**
     * 别名
     * 整个表达后的别名
     * 默认为字段名
     *
     * 如果为 "NULL" 值，则没有别名
     *
     * @return
     */
    String alias() default "";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "语句表达式生成规则： surroundPrefix + op.gen( fieldFuncs( fieldCases(domain.fieldName) ), paramFuncs( fieldCases([ paramExpr(优先) or 参数占位符 ])) ) +  surroundSuffix";

}
