package com.levin.commons.dao.annotation;

import com.levin.commons.annotation.GenAnnotationMethodNameConstant;

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
 * 字段名或是注解的Value值，变量名称：_FIELD_NAME
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
 * 4、整个语句产生后最后再进行字符串内容的替换，字符串内容替换使用表达为 ${}，比如${_FIELD_NAME} --> 会被替换成字段名
 * <p>
 * <p>
 * 例子：
 * <p>
 * select * from ${table} t where  t.name like  ${:name} and t.age > :age and t.sex = :? and t.weight > ${weight}
 *
 * @author llw
 * @@since 2.1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@GenAnnotationMethodNameConstant
public @interface C {

    /**
     * 不是 NUll 对象 ，也不是空字符串
     */
    String NOT_NULL = "#_val != null and (!(#_val instanceof T(CharSequence)) ||  #_val.trim().length() > 0)";


    /**
     * 操作
     *
     * @return
     */
    Op op() default Op.Eq;


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
    String condition() default NOT_NULL;

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
