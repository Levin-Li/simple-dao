package com.levin.commons.dao.annotation.order;

import com.levin.commons.dao.annotation.misc.Case;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * OrderBy注解
 *
 * 被注解字段必须是字符串或是字符数组
 *
 * 排序
 * eg:
 *      @SimpleOrderBy(condition = "state.length > 0")
 *      String[] orderBy = {"state desc", "name asc"};
 *
 *      @SimpleOrderBy(condition = "name != null")
 *      String orderBy2 = "score desc , category asc";
 *
 *      @SimpleOrderBy(condition = "name != null", expr="score desc")
 *      String name = "";
 *
 * @author llw
 * @version 2.0.0
 */
public @interface SimpleOrderBy {

    /**
     * 排序语句表达式
     * 默认 SPEL 表达式
     *
     * @return
     */
    String expr() default "";

    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition() default "";

    /**
     * 排序优先级
     * <p/>
     * 按数值从大到小排序
     * <p/>
     *
     * @return
     */
    int order() default 0;

    /**
     * 描述信息
     *
     * @return
     */
    String remark() default "";

}
