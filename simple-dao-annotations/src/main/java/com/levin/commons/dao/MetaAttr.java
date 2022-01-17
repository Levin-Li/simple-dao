package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 元属性处理注解
 *
 * @author llw
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MetaAttr {

    /**
     * 属性处理器
     *
     * @return
     */
    Class<AttrHandler>[] value() default {};

    /**
     * 触发条件
     *
     * @return
     */
    String condition() default "";

    /**
     * 没有值时，是否抛出异常
     *
     * @return
     */
    boolean isThrowExWhenNotValue() default false;

}
