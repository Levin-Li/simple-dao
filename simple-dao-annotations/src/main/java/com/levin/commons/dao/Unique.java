package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 唯一标识注解
 * <p>
 * 辅助的保存和编辑时提示唯一约束
 * <p>
 * 在数据库定义的唯一约束之外，提供额外的定义方式，用于处理，数据库不定义唯一约束，但程序要保证唯一的情况
 *
 * @author llw
 * @version 2.3.2
 */
@Repeatable(Unique.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Unique {

    /**
     * 字段名列表
     * 默认为被注解字段名称
     */
    String[] value() default "";

    /**
     * 分组
     */
    String group() default "";


    /**
     * 错误提示
     *
     * @return
     */
    String prompt() default "";

    /**
     * 备注
     */
    String remark() default "";

    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface List {
        /**
         * 注解列表
         *
         * @return
         */
        Unique[] value();
    }
}
