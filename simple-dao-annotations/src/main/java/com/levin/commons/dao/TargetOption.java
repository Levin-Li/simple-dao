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
     * 查询期望的查询结果类
     * 只针对查询有效
     *
     * @return
     */
//    Class ResultClass() default Void.class;

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
     * 是否是安全模式
     * <p>
     * 安全模式时，不允许无条件的更新
     *
     * @return
     */
    boolean isSafeMode() default true;


    /**
     * 查询的最大结果集记录数
     *
     * @return
     */
    int maxResults() default -1;


    /**
     * 固定条件
     *
     * @return
     */
    String fixedCondition() default "";

}
