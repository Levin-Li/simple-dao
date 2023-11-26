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
     * 数据类型
     *
     * @return
     */
    String value();

    /**
     * 描述
     *
     * @return
     */
    String desc() default "";

}
