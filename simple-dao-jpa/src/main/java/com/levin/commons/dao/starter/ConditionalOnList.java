package com.levin.commons.dao.starter;


import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(SimpleCondition.class)
public @interface ConditionalOnList {

    /**
     * 是否要求所有的条件都匹配
     * 默认匹配所有条件
     *
     * @return
     */
    boolean requireAllMatch() default true;


    /**
     * 条件列表
     *
     * @return
     */
    ConditionalOn[] value();

}
