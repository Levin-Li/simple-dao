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
    }

    /**
     * 默认属性
     *
     * @return
     */
    String value() default "";


    /**
     * 字段归属的域，通常是表的别名
     *
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
     * 要抓取属性列表
     * <p>
     * use value
     * <p>
     * 该属性只对查询对象有效，对结果对象（find(clazzType) 方法的参数类型对象）无效
     *
     * @return
     */
    String[] attrs() default {};

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
