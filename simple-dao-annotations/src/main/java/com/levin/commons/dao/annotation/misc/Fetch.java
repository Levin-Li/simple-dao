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
 * 仅适用于JPA
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
     * 字段关联的属性名称
     * 当 被注解的 DTO 是结果对象时，才有效
     *
     * @return
     */
    String value() default "";

    /**
     * 需要连接抓取的属性列表
     * <p>
     * use value
     * <p>
     * 该属性只对查询对象有效，对结果对象（find(clazzType) 方法的参数类型对象）无效
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
     * 连接类型
     *
     * @return
     */
    JoinType joinType() default JoinType.Left;


    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default "";


    /**
     * @return
     */
    String desc() default "";

}
