package com.levin.commons.dao.annotation.misc;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 抓取集合属性
 *
 * 该注解主要应用于查询对象，结果对象也可以使用，但必须 设置 onlyForQueryObject 为 false.
 *
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Fetch {

    enum JoinType {
        Left,
        Right,
        Inner,
        Natural,
        Full,
        None
    }

    /**
     * 需要被拷贝目标属性名称
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
     * 当 onlyForQueryObject 为 false 时，这个属性会被关联抓取
     */
    String value() default "";


    /**
     * 默认对查询对象会产生 JPA 的 join fetch 指令，对结果对象需要特别 把 onlyForQueryObject 设置为 false 才会生成join fetch 指令。
     *
     * @return
     */
    boolean onlyForQueryObject() default true;

    /**
     * 需要连接抓取的属性列表
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
