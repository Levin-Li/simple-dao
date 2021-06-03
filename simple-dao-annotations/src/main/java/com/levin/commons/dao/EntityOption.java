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

/*

类继承关系中@Inherited的作用
类继承关系中，子类会继承父类使用的注解中被@Inherited修饰的注解
接口继承关系中@Inherited的作用
接口继承关系中，子接口不会继承父接口中的任何注解，不管父接口中使用的注解有没有被@Inherited修饰
类实现接口关系中@Inherited的作用
类实现接口时不会继承任何接口中定义的注解

*/
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

    //////////////////////////////////////////////////////////////


    /**
     * 逻辑删除记录的判定语句
     *
     *
     * <p>
     * 如  status = 'Deleted' AND  enable = 'False'
     * @todo 涉及到字段别名问题，暂不支持
     * @return
     */

//    String logicalDeleteDeterminedStatement() default "";


    /**
     * 逻辑删除的设置语句，多个字段之间逗号隔开
     *
     *
     * <p>
     * 如  status = 'Deleted' , enable = 'False'
     * @todo 涉及到字段别名问题，暂不支持
     * @return
     */
//    String logicalDeleteSetValueStatement() default "";

    /////////////////////////////////////////////////////////////////////

    /**
     * 注意 优先级 低于 logicalDeleteDeterminedStatement
     * 逻辑删除的字段名
     * <p>
     * 该字段一般不允许空值
     *
     * @return
     */
    String logicalDeleteFieldName() default "";


    /**
     * 逻辑删除的值
     * dao 会自动根据字段类型进行值转换
     *
     * @return <p>
     * 如： state= 'deleted'
     */
    String logicalDeleteValue() default "";


    /**
     * @return
     */
    String desc() default "逻辑删除的自定义语句优先级高于指定字段的方式，注意逻辑删除，并不影响连接查询";

}
