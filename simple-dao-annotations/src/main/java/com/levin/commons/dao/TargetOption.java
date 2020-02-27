package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * 参数注解优先--> 方法注解 --> 类注解 --> 包注解
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TargetOption {

    /**
     * 默认查询的目标实体类
     *
     * @return
     */
    Class entityClass() default Void.class;

    /**
     * 表名
     *
     * @return
     */
    String tableName() default "";


    /**
     * 别名
     * <p>
     * 针对单个查询目标时有效
     *
     * @return
     */
    String alias() default "";


    /**
     * from 字句
     *
     * @return
     */
    String fromStatement() default "";


    /**
     * 有效记录的开始位置
     * <p/>
     * 主要用于分页
     *
     * @return
     */
    int startIndex() default -1;


    /**
     * 最大结果数
     *
     * @return
     */
    int maxResults() default -1;


    /**
     * 固定条件
     * 如 enable = true
     *
     * @return
     */
    String fixedCondition() default "";

}
