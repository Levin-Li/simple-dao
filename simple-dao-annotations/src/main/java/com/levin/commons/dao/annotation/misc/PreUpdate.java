package com.levin.commons.dao.annotation.misc;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

public @interface PreUpdate {

    /**
     * 生效条件
     *
     * @return
     */
    String condition() default "";

    /**
     * 描述
     *
     * @return
     */
    String desc() default "";

}
