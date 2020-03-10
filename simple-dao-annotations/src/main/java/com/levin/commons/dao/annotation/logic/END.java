package com.levin.commons.dao.annotation.logic;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 * 逻辑分组结束注解，这个注解不生产语句，只是表示分组结束
 *
 * @author llw
 * @version 2.0.0
 */
public @interface END {

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "分组结束";

}
