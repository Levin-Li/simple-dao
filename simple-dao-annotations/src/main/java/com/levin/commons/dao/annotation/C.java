package com.levin.commons.dao.annotation;


import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.annotation.misc.Case;

import java.lang.annotation.*;

/**
 * 语句注解
 * <p>
 * <p>
 * 如果被注解的对象是一个查询对象，产生递归的子查询
 *
 * <p>
 * 环境变量说明：
 * DaoContext 全局环境参数（Map类型），变量名称：_G
 * DaoContext 线程环境参数（Map类型），变量名称：_T
 * 当前Dao环境参数（Map类型），变量名称：_dao
 * <p>
 * <p>
 * 被注解的字段值变量名称：_val
 * 字段名或是注解的Value值，变量名称：_name
 * <p>
 * 语句替换规则
 * <p>
 * 1、参数替换规则：${:paramName} --> 会被替换成JPA 索引参数样式
 * <p>
 * 2、免替换规则：:paramName --> JPA 命名参数样式
 * :?         --> JPA 索引参数样式
 * <p>
 * 3、字符串替换：${paramName}
 * <p>
 * 4、整个语句产生后最后再进行字符串内容的替换，字符串内容替换使用表达为 ${}，比如${_name} --> 会被替换成字段名
 * <p>
 * <p>
 * 例子：
 * <p>
 * select * from ${table} t where  t.name like  ${:name} and t.age > :age and t.sex = :? and t.weight > ${weight}
 *
 * @author llw
 * @@since 2.1.0
 */
@Repeatable(C.List.class)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@GenNameConstant
public @interface C {

    /**
     * 首先不是 NUll 对象 ，也不是空字符串
     * 如果是数组或是集合或是 Map 也不是空
     */
    String VALUE_EMPTY = "VALUE_EMPTY";

    String VALUE_NOT_EMPTY = "VALUE_NOT_EMPTY";

    @Deprecated
    String NOT_EMPTY = "NOT_EMPTY";

    /**
     * 特别空值
     * 对于domain 和 alias 可强行指定空值
     */
    String BLANK_VALUE = "#BLANK";

    /**
     * 原表达式
     */
    String ORIGIN_EXPR = "$$";

    /**
     * 替换字段前缀
     * <p>
     * 单原生查询时，可以用对象的属性自动转换成数据库列名
     * <p>
     * 如：
     * F$:u.userId
     * 原生查询 --->   u.user_id
     * JPA查询 --->   u.userId
     */
    String FIELD_PREFIX = "F$:";

    /**
     * 操作
     *
     * @return
     */
    Op op() default Op.Eq;

    /**
     * 字段归属的域，通常是表的别名
     * <p>
     * 如果为 "NULL" 值，则忽略这个值
     * <p>
     * 该字段支持 spel 表达式，使用 SPEL_PREFIX 做为前缀时
     * 支持动态前缀
     *
     * @return
     */
    String domain() default "";

    /**
     * 查询字段名称，默认为字段的属性名称
     *
     * <p>
     * 对应数据库的字段名或是 Jpa 实体类的字段名
     * <p>
     * 通常代表左操作数
     * <p>
     * 也就是代表字段
     *
     * @return
     */
    String value() default "";


    /**
     * 是否为 操作数（既字段） value 变量值 自动增加别名前缀
     *
     * @return
     */
    boolean isAddAliasPrefixForValue() default true;


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
    String condition() default NOT_EMPTY;


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
     * 最后一个函数有效
     * <p>
     * 但可以在 params 参数用 DEFAULT_PARAM 嵌套前一个函数的表达式
     *
     * <p>
     * 也是左操作数
     *
     * <p>
     *
     * <p>
     * <p>
     * 如果是更新字段则忽略
     *
     * @return
     */
    Func[] fieldFuncs() default {};

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
     * 也是右操作数
     * 最后一个函数有效
     * <p>
     * 但可以在 params 参数用 DEFAULT_PARAM 嵌套前一个函数的表达式
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
     * 子查询或是表达式
     * <p>
     * 如果配置这个属性，将覆盖原有的占位符表达式
     * <p>
     * 通常代表右操作数
     *
     * @return
     */
    String paramExpr() default "";

    /**
     * 对当前字段转换成字符串的模板
     * <p>
     * 通常是 group by 时间字段时使用
     * <p>
     * 支持格式 y m d h m s 代表年 月 日 时 分 秒
     *
     * @return
     */
    //暂时不支持
    //String toCharPattern() default "";

    /**
     * 数据类型转换模板
     * 通常是date类型转换 ，如： yyyy-MM-dd
     *
     * @return
     */
    String[] patterns() default {};


    /**
     * 参数之间的分隔符，仅对参数是字符串时有效
     * 如
     * <p>
     * 通常用于时间，如： 2020-01-01|2020-03-01
     *
     * @return
     */
    String paramDelimiter() default "";


    /**
     * 对整个表达式的包围后缀
     *
     * @return
     */
    String surroundSuffix() default "";


    /**
     * 描述信息
     * 表达式的基本形态： 左操作数  +  操作  + 右操作数
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
        C[] value();
    }
}
