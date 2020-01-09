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
     * 逻辑范围条件结束
     *
     * @return
     */
    String value() default "";


    /**
     * 操作符
     *
     * @return
     */
    String op() default "  ";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix() default "  ";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix() default "  ";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "分组结束";

}
