package com.levin.commons.dao.annotation.misc;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 抓取集合属性
 *
 * 该注解主要应用于查询对象，结果对象也可以使用，但必须 设置 isBindToField 为 true.
 *
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Fetch {

    /**
     * 自然连接、内连接、外连接（左外连接、右外连接、全外连接）、交叉连接
     *
     */
    enum JoinType {

        @Schema(description = "自然连接不用指定连接列，也不能使用ON语句，它默认比较两张表里相同的名字的列，eg. A NATURAL JOIN B ")
        Natural,

        @Schema(description = "内连接和自然连接区别之处在于内连接可以自定义两张表的不同列字段，内连接有两种形式：显式和隐式。" +
                "隐式的内连接，没有INNER JOIN，形成的中间表为两个表的笛卡尔积。 " +
                "显示的内连接，一般称为内连接，有INNER JOIN，形成的中间表为两个表经过ON条件过滤后的笛卡尔积。")
        Inner,

        Left,

        Right,
        @Schema(description = "全外连接（full outer join）：把左右两表进行自然连接，左表在右表没有的显示NULL，右表在左表没有的显示NULL。")
        Full,

        @Schema(description = "相当与笛卡尔积，左表和右表组合。")
        Cross,

        None
    }

    /**
     * 抓取的属性名称
     *
     * <p>
     * 被注解的字段值，将从目标对象的拷贝，当 value 有指定值时，将拷贝 value 指定的属性值。
     * <p>
     * 例子：
     * <p>
     * class User {
     * Group group;
     * ...
     * }
     * <p>
     * class UserInfo{
     *
     * @return
     * @Fetch(value ="group.name")  //将从目标 User 对象重 提取 group 的 name 属性，赋值到 groupName字段上
     * String groupName;
     * }
     *
     *
     * <p>
     * 当 isBindToField 为 false 时，这个属性会被关联抓取
     */
    String value() default "";


    /**
     * 是否绑定到当前字段
     * <p>
     * false 不绑定到字段，只是设置抓取
     *
     * @return
     */
    boolean isBindToField() default true;

    /**
     * 需要连接抓取的属性列表
     *
     * <p>
     * use value
     * <p>
     * 该属性只对查询对象有效，对结果对象（find(resultType) 方法的参数类型对象）无效
     *
     * @return
     */
    String[] attrs() default {};

    /**
     * 字段归属的域，通常是表的别名
     * <p>
     * 暂时不支持
     *
     * @return
     */
    String domain() default "";

    /**
     * 是否是必须的
     *
     * @return
     */
    boolean require() default false;


    /**
     * 暂时仅对查询对象有效，注解在结果对象上时，该属性无效
     * <p>
     * 生效表达式
     * <p>
     * 目前支持 SpEL
     * <p/>
     * 当条件成立时，注解的 jpa join fetch 功能才会生效，否则 不会产生 join fetch 指令。
     *
     * @return
     */
    String condition() default "";

    /**
     * 连接类型
     * 默认是左连接
     *
     * @return
     */
    JoinType joinType() default JoinType.Left;


    /**
     * @return
     */
    String desc() default "";

}
