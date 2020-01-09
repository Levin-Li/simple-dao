package com.levin.commons.dao.annotation.misc;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 *
 *  用于处理整个对象的逻辑验证，不针对单个属性
 *
 * @author llw
 * @version 2.0.0
 */
public @interface Validator {

    /**
     * 标注的字段名
     *
     * @return
     */
    String value() default "";

    /**
     * 验证表达式，目前支持 SPEL
     *
     * @return
     */
    String expr();

    /**
     * 验证异常时的提示说明
     *
     * @return
     */
    String promptInfo();


    /**
     * @return
     */
    String desc() default "";

}
