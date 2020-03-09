package com.levin.commons.dao.annotation.update;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 不可变的属性
 *
 * 用于注解在实体上
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Immutable {

    /**
     * 默认属性
     *
     * @return
     */
    String value() default "";

    /**
     * 提示说明
     *
     * @return
     */
    String remark();

}
