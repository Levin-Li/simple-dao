package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 数据类型
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EntityCategory {

    /**
     * 数据类型名称或是编码，如 系统数据，通用数据等。
     * <p>
     * 参考以下定义类
     *
     * @return
     * @see EntityOpConst
     */
    String value();

    /**
     * 代码生产的时候，查询对象继承的类型。
     * <p>
     * 查询对象继承的类型，默认为 Void.class，自动选择
     *
     * @return
     */
    Class<?> queryObjectExtendType() default Void.class;

    /**
     * 描述
     *
     * @return
     */
    String desc() default "";

}
