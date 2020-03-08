package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/**
 * 语句注解集合
 *
 * @author llw
 * @@since 2.1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CList {

    /**
     * @return
     */
    C[] value() default {};

}
