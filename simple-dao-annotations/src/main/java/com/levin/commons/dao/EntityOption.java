package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 实体可选项
 * <p>
 * 用于标注实体类
 *
 * @author levin li
 * @since 2.2.11
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EntityOption {

    /**
     * Dao 实体操作
     */
    enum Action {

        /**
         * 可创建
         */
        Create,

        /**
         * 可读
         */
        Read,

        /**
         * 可修改
         */
        Update,

        /**
         * 可逻辑删除
         * <p>
         * 通过标记某个字段为特定值表示删除
         */
        LogicalDelete,

        /**
         * 可物理删除
         */
        Delete;

    }


    /**
     * 禁止的动作列表
     *
     * @return
     */
    Action[] disableActions() default {};

    /**
     * 逻辑删除的字段
     * <p>
     * 该字段一般不允许空值
     *
     * @return
     */
    String logicalDeleteField() default "";


    /**
     * 逻辑删除的值
     * dao 会自动根据字段类型进行值转换
     *
     * @return
     */
    String logicalDeleteValue() default "";

}
