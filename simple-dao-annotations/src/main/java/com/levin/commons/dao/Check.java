package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 字段或是类检查
 *
 *
 * <p>
 *
 * @author levin li
 * @since 2.2.30
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Check {

    /**
     * 字段注解或是类注解上，where 条件必须满足的数量
     *
     * @return
     */
    int requireWhereCount() default 0;


    /**
     * 必须满足
     *
     * @return
     */
    Class<? extends Annotation>[] requireAnnotations() default {};

    /**
     * 备注
     *
     * @return
     */
    String remark() default "";

}
