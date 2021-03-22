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
     * 可更新记录的条件
     * <p>
     * <p>
     * 所有的更新语句都会加上这个条件
     * 细粒度控制
     * <p>
     * 比如 editable == true
     *
     * <p>
     * 默认没有条件
     *
     * @return
     */
    String updateCondition() default "";


    /**
     * 可删除记录的条件
     * <p>
     * <p>
     * 所有的删除语句都会加上这个条件
     * 细粒度控制
     * <p>
     * 比如 status != '已使用'
     *
     * <p>
     * 默认没有条件
     *
     * @return
     */
    String deleteCondition() default "";


    /**
     * 逻辑删除的字段名
     * <p>
     * 该字段一般不允许空值
     *
     * @return
     * @todo 抽象为逻辑删除的可见表达式
     */
    String logicalDeleteFieldName() default "";


    /**
     * 逻辑删除的值
     * dao 会自动根据字段类型进行值转换
     *
     * @return
     * @todo 抽象为逻辑删除的设值表达式
     * <p>
     * 如： state= 'deleted'
     */
    String logicalDeleteValue() default "";

}
