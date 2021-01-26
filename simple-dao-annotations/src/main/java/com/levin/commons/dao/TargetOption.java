package com.levin.commons.dao;

import lombok.experimental.FieldNameConstants;

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
    Class resultClass() default Void.class;

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
     * <p>
     * 更新和删除操作，此属性无意义
     *
     * @deprecated 使用joinOptions 替代
     * @return
     */
    @Deprecated
    String fromStatement() default "";


    /**
     * 连接选项
     * <p>
     * 改配置的优先级高于 fromStatement属性，会让fromStatement属性失效
     * <p>
     * 更新和删除操作，此属性无意义
     *
     * @return
     */
    JoinOption[] joinOptions() default {};

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
    //@Deprecated
    //String fixedCondition() default "";

}
