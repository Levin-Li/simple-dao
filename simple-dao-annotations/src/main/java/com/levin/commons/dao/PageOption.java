package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 分页支持注解
 * <p>
 * 用于兼容现有的类设计
 *
 * <p>
 *
 * @author levin li
 * @since 2.2.10
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PageOption {

    enum Type {
        /**
         * 总记录总数
         */
        RequireTotals,
        /**
         * //查询结果集
         */
        RequireResultList,

        /**
         * 页面大小
         */
        PageSize,
        /**
         * //排序字段
         */
        PageIndex,
    }

    /**
     * 字段类型
     *
     * @return
     */
    Type value();

    /**
     * 注解生效条件
     * SPEL 表达式
     *
     * @return
     */
    String condition() default "";

    /**
     * 备注
     *
     * @return
     */
    String remark() default "";

}
